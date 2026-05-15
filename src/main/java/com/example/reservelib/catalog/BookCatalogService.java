package com.example.reservelib.catalog;

import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.catalog.dto.LibrariesBatchCheckResponse;
import com.example.reservelib.catalog.dto.LibraryCheckFailure;
import com.example.reservelib.catalog.dto.LibraryCheckResponse;
import com.example.reservelib.catalog.dto.LibraryRefreshStatusResponse;
import com.example.reservelib.irbis.IrbisService;
import com.example.reservelib.irbis.dto.IrbisLibrariesResponse;
import com.example.reservelib.model.Book;
import com.example.reservelib.model.BookRowMapper;
import com.example.reservelib.model.LibraryRowMapper;
import com.example.reservelib.util.DateTimeView;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookCatalogService {
    private static final long LIBRARIES_SYNC_TTL_DAYS = 5L;

    private final JdbcTemplate jdbcTemplate;
    private final IrbisService irbisService;
    private final BookRowMapper bookRowMapper = new BookRowMapper();
    private final LibraryRowMapper libraryRowMapper = new LibraryRowMapper();

    public BookCatalogService(JdbcTemplate jdbcTemplate, IrbisService irbisService) {
        this.jdbcTemplate = jdbcTemplate;
        this.irbisService = irbisService;
    }

    public List<Book> searchBooksByTitle(String title) {
        List<Book> books = jdbcTemplate.query("""
                SELECT id, title, author, isbn, genre, description, last_libraries_sync_at
                FROM books
                WHERE title ILIKE ?
                ORDER BY title
                """, bookRowMapper, "%" + title.trim() + "%");
        fillLibraryNames(books);
        return books;
    }

    public List<Book> getAllBooks() {
        List<Book> books = jdbcTemplate.query("""
                SELECT id, title, author, isbn, genre, description, last_libraries_sync_at
                FROM books
                ORDER BY id
                """, bookRowMapper);
        fillLibraryNames(books);
        return books;
    }

    public Book getBookById(Long id) {
        List<Book> books = jdbcTemplate.query("""
                SELECT id, title, author, isbn, genre, description, last_libraries_sync_at
                FROM books
                WHERE id = ?
                """, bookRowMapper, id);
        if (books.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        fillLibraryNames(books);
        return books.getFirst();
    }

    @Transactional
    public Book createBook(BookRequest request) {
        validateRequest(request);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO books (title, author, isbn, genre, description)
                VALUES (?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.getTitle().trim());
            ps.setString(2, request.getAuthor());
            ps.setString(3, request.getIsbn());
            ps.setString(4, request.getGenre());
            ps.setString(5, request.getDescription());
            return ps;
        }, keyHolder);

        Long id = (Long) keyHolder.getKey();
        syncBookLibrariesByNames(id, request.getLibraryNames());
        return getBookById(id);
    }

    @Transactional
    public Book updateBook(Long id, BookRequest request) {
        ensureBookExists(id);
        validateRequest(request);
        jdbcTemplate.update("""
                UPDATE books
                SET title = ?, author = ?, isbn = ?, genre = ?, description = ?
                WHERE id = ?
                """, request.getTitle().trim(), request.getAuthor(), request.getIsbn(), request.getGenre(), request.getDescription(), id);
        syncBookLibrariesByNames(id, request.getLibraryNames());
        return getBookById(id);
    }

    public void deleteBook(Long id) {
        int updated = jdbcTemplate.update("DELETE FROM books WHERE id = ?", id);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
    }

    @Transactional
    public void refreshLibrariesByBookId(Long id) {
        Book book = getBookById(id);
        IrbisLibrariesResponse response = irbisService.fetchLibrariesByTitle(book.getTitle());
        saveIrbisLibrariesForBook(id, response.libraries());
    }

    public LibraryCheckResponse checkLibrariesByBookId(Long id) {
        Book book = getBookById(id);
        LocalDateTime lastSyncAt = book.getLastLibrariesSyncAt();
        IrbisLibrariesResponse libraries;

        if (isLibrariesSyncStale(lastSyncAt)) {
            libraries = irbisService.fetchLibrariesByTitle(book.getTitle());
            saveIrbisLibrariesForBook(id, libraries.libraries());
            lastSyncAt = getLastLibrariesSyncAt(id);
        } else {
            List<String> libraryNames = book.getLibraryNames().stream()
                    .sorted()
                    .toList();
            libraries = new IrbisLibrariesResponse(book.getTitle(), libraryNames.size(), libraryNames);
        }

        return new LibraryCheckResponse(
                id,
                book.getTitle(),
                DateTimeView.moscowHour(lastSyncAt),
                libraries.count(),
                libraries.libraries()
        );
    }

    public LibrariesBatchCheckResponse checkStaleLibraries() {
        List<Long> bookIds = jdbcTemplate.query("""
                SELECT id
                FROM books
                WHERE last_libraries_sync_at IS NULL
                   OR last_libraries_sync_at <= (CURRENT_TIMESTAMP - INTERVAL '5 days')
                ORDER BY id
                """, (rs, rowNum) -> rs.getLong("id"));

        List<LibraryCheckResponse> results = new ArrayList<>();
        List<LibraryCheckFailure> failed = new ArrayList<>();
        for (Long bookId : bookIds) {
            try {
                results.add(checkLibrariesByBookId(bookId));
            } catch (Exception e) {
                failed.add(new LibraryCheckFailure(bookId, e.getMessage()));
            }
        }

        return new LibrariesBatchCheckResponse(
                bookIds.size(),
                results.size(),
                failed.size(),
                results,
                failed
        );
    }

    public LibraryRefreshStatusResponse getLibrariesRefreshStatus(Long id) {
        ensureBookExists(id);
        LocalDateTime updatedAt = getLastLibrariesSyncAt(id);
        return new LibraryRefreshStatusResponse(id, isLibrariesSyncStale(updatedAt), DateTimeView.moscowHour(updatedAt));
    }

    private LocalDateTime getLastLibrariesSyncAt(Long id) {
        Timestamp syncedAt = jdbcTemplate.query("""
                SELECT last_libraries_sync_at
                FROM books
                WHERE id = ?
                """, rs -> rs.next() ? rs.getTimestamp("last_libraries_sync_at") : null, id);
        return syncedAt == null ? null : syncedAt.toLocalDateTime();
    }

    private boolean isLibrariesSyncStale(LocalDateTime lastSyncAt) {
        return lastSyncAt == null || !lastSyncAt.isAfter(LocalDateTime.now().minusDays(LIBRARIES_SYNC_TTL_DAYS));
    }

    private void saveIrbisLibrariesForBook(Long bookId, List<String> libraries) {
        syncBookLibrariesByNames(bookId, libraries);
        jdbcTemplate.update("""
                UPDATE books
                SET last_libraries_sync_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, bookId);
    }

    private void ensureBookExists(Long id) {
        Long exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE id = ?", Long.class, id);
        if (exists == null || exists == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
    }

    @Transactional
    private void syncBookLibrariesByNames(Long bookId, List<String> libraryNames) {
        jdbcTemplate.update("DELETE FROM book_libraries WHERE book_id = ?", bookId);
        for (String name : normalizeLibraryNames(libraryNames)) {
            jdbcTemplate.update("""
                    INSERT INTO libraries (name)
                    VALUES (?)
                    ON CONFLICT (name) DO NOTHING
                    """, name);
            Long libraryId = jdbcTemplate.queryForObject("SELECT id FROM libraries WHERE name = ?", Long.class, name);
            jdbcTemplate.update("""
                    INSERT INTO book_libraries (book_id, library_id)
                    VALUES (?, ?)
                    ON CONFLICT (book_id, library_id) DO NOTHING
                    """, bookId, libraryId);
        }
    }

    private List<String> normalizeLibraryNames(List<String> names) {
        if (names == null) {
            return Collections.emptyList();
        }
        return names.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private void validateRequest(BookRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
    }

    private void fillLibraryNames(List<Book> books) {
        if (books.isEmpty()) {
            return;
        }

        Map<Long, Book> byId = new HashMap<>();
        for (Book book : books) {
            book.setLibraryNames(new java.util.ArrayList<>());
            byId.put(book.getId(), book);
        }

        String placeholders = books.stream()
                .map(book -> "?")
                .collect(Collectors.joining(", "));
        Object[] ids = books.stream()
                .map(Book::getId)
                .toArray();

        jdbcTemplate.query("""
                SELECT bl.book_id, l.name
                FROM book_libraries bl
                JOIN libraries l ON l.id = bl.library_id
                WHERE bl.book_id IN (%s)
                ORDER BY bl.book_id, l.name
                """.formatted(placeholders), rs -> {
            Long bookId = rs.getLong("book_id");
            Book book = byId.get(bookId);
            if (book != null) {
                book.getLibraryNames().add(rs.getString("name"));
            }
        }, ids);
    }
}