package com.example.reservelib.catalog;

import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.model.Book;
import com.example.reservelib.model.BookRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;
    private final BookRowMapper bookRowMapper = new BookRowMapper();

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countBooks(String title, String author, String genre) {
        StringBuilder countSql = new StringBuilder("SELECT count(*) FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            countSql.append(" AND title ILIKE ?");
            params.add("%" + title.trim() + "%");
        }
        if (author != null && !author.isBlank()) {
            countSql.append(" AND author ILIKE ?");
            params.add("%" + author.trim() + "%");
        }
        if (genre != null && !genre.isBlank()) {
            countSql.append(" AND genre ILIKE ?");
            params.add("%" + genre.trim() + "%");
        }

        Long count = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        return count == null ? 0L : count;
    }

    public List<Book> findBooks(String title, String author, String genre, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT id, title, author, isbn, genre, description, last_libraries_sync_at FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            sql.append(" AND title ILIKE ?");
            params.add("%" + title.trim() + "%");
        }
        if (author != null && !author.isBlank()) {
            sql.append(" AND author ILIKE ?");
            params.add("%" + author.trim() + "%");
        }
        if (genre != null && !genre.isBlank()) {
            sql.append(" AND genre ILIKE ?");
            params.add("%" + genre.trim() + "%");
        }

        sql.append(" ORDER BY id ASC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbcTemplate.query(sql.toString(), bookRowMapper, params.toArray());
    }

    public Optional<Book> findById(Long id) {
        List<Book> books = jdbcTemplate.query("""
                SELECT id, title, author, isbn, genre, description, last_libraries_sync_at
                FROM books
                WHERE id = ?
                """, bookRowMapper, id);
        if (books.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(books.getFirst());
    }

    public Long create(BookRequest request) {
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
        return (Long) keyHolder.getKey();
    }

    public void update(Long id, BookRequest request) {
        jdbcTemplate.update("""
                UPDATE books
                SET title = ?, author = ?, isbn = ?, genre = ?, description = ?
                WHERE id = ?
                """, request.getTitle().trim(), request.getAuthor(), request.getIsbn(), request.getGenre(), request.getDescription(), id);
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM books WHERE id = ?", id);
    }

    public List<Long> findStaleBookIds() {
        return jdbcTemplate.query("""
                SELECT id
                FROM books
                WHERE last_libraries_sync_at IS NULL
                   OR last_libraries_sync_at <= (CURRENT_TIMESTAMP - INTERVAL '5 days')
                ORDER BY id
                """, (rs, rowNum) -> rs.getLong("id"));
    }

    public LocalDateTime getLastLibrariesSyncAt(Long id) {
        Timestamp syncedAt = jdbcTemplate.query("""
                SELECT last_libraries_sync_at
                FROM books
                WHERE id = ?
                """, rs -> rs.next() ? rs.getTimestamp("last_libraries_sync_at") : null, id);
        return syncedAt == null ? null : syncedAt.toLocalDateTime();
    }

    public void updateLastLibrariesSyncAt(Long id) {
        jdbcTemplate.update("""
                UPDATE books
                SET last_libraries_sync_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, id);
    }

    public boolean existsById(Long id) {
        Long exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE id = ?", Long.class, id);
        return exists != null && exists > 0;
    }

    public void clearBookLibraries(Long bookId) {
        jdbcTemplate.update("DELETE FROM book_libraries WHERE book_id = ?", bookId);
    }

    public void insertLibraryIfNotExists(String name) {
        jdbcTemplate.update("""
                INSERT INTO libraries (name)
                VALUES (?)
                ON CONFLICT (name) DO NOTHING
                """, name);
    }

    public Long findLibraryIdByName(String name) {
        return jdbcTemplate.queryForObject("SELECT id FROM libraries WHERE name = ?", Long.class, name);
    }

    public void linkBookAndLibrary(Long bookId, Long libraryId) {
        jdbcTemplate.update("""
                INSERT INTO book_libraries (book_id, library_id)
                VALUES (?, ?)
                ON CONFLICT (book_id, library_id) DO NOTHING
                """, bookId, libraryId);
    }

    public void fillLibraryNames(List<Book> books) {
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