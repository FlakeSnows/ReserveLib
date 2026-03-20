package com.example.reservelib.catalog;

import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.catalog.dto.LibraryResponse;
import com.example.reservelib.model.Book;
import com.example.reservelib.model.BookRowMapper;
import com.example.reservelib.model.LibraryRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BookCatalogService {

    private final JdbcTemplate jdbcTemplate;
    private final BookRowMapper bookRowMapper = new BookRowMapper();
    private final LibraryRowMapper libraryRowMapper = new LibraryRowMapper();

    public BookCatalogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Book> getAllBooks() {
        String sql = "SELECT * FROM books ORDER BY id";
        List<Book> books = jdbcTemplate.query(sql, bookRowMapper);
        books.forEach(this::loadLibraries);
        return books;
    }

    public Book getBookById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        List<Book> books = jdbcTemplate.query(sql, bookRowMapper, id);
        if (books.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Book not found");
        }
        Book book = books.getFirst();
        loadLibraries(book);
        return book;
    }

    public List<LibraryResponse> getLibrariesByBookId(int bookId) {
        getBookById(bookId);

        String sql = """
                SELECT libraries.id, libraries.name, libraries.address, libraries.latitude, libraries.longitude
                FROM libraries
                JOIN book_libraries ON book_libraries.library_id = libraries.id
                WHERE book_libraries.book_id = ?
                ORDER BY libraries.id
                """;

        return jdbcTemplate.query(sql, libraryRowMapper, bookId);
    }

    public Book createBook(BookRequest request) {
        List<Integer> libraryIds = resolveLibraryIds(request);

        String sql = "INSERT INTO books (title, author, isbn, genre, description) VALUES (?, ?, ?, ?, ?) RETURNING id";
        Integer id = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getGenre(),
                request.getDescription()
        );

        insertBookLibraries(id, libraryIds);
        return getBookById(id);
    }

    public Book updateBook(int id, BookRequest request) {
        List<Integer> libraryIds = resolveLibraryIds(request);

        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, genre = ?, description = ? WHERE id = ?";
        int updated = jdbcTemplate.update(
                sql,
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getGenre(),
                request.getDescription(),
                id
        );
        if (updated == 0) {
            throw new ResponseStatusException(NOT_FOUND, "Book not found");
        }

        String deleteSql = "DELETE FROM book_libraries WHERE book_id = ?";
        jdbcTemplate.update(deleteSql, id);
        insertBookLibraries(id, libraryIds);
        return getBookById(id);
    }

    public void deleteBook(int id) {
        String sql = "DELETE FROM books WHERE id = ?";
        int deleted = jdbcTemplate.update(sql, id);
        if (deleted == 0) {
            throw new ResponseStatusException(NOT_FOUND, "Book not found");
        }
    }

    private List<Integer> resolveLibraryIds(BookRequest request) {
        if (request.getLibraryNames() == null || request.getLibraryNames().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "libraryNames is required");
        }

        Set<Integer> resolved = new LinkedHashSet<>();
        for (String libraryName : request.getLibraryNames()) {
            if (libraryName == null || libraryName.isBlank()) {
                throw new ResponseStatusException(BAD_REQUEST, "libraryNames must not contain blank values");
            }
            resolved.add(requireLibraryByName(libraryName.trim()));
        }
        return new ArrayList<>(resolved);
    }

    private Integer requireLibraryByName(String libraryName) {
        try {
            String sql = "SELECT id FROM libraries WHERE name = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, libraryName);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResponseStatusException(NOT_FOUND, "Library not found: " + libraryName);
        }
    }

    private void insertBookLibraries(Integer bookId, List<Integer> libraryIds) {
        String sql = "INSERT INTO book_libraries (book_id, library_id) VALUES (?, ?)";
        for (Integer libraryId : libraryIds) {
            jdbcTemplate.update(sql, bookId, libraryId);
        }
    }

    private void loadLibraries(Book book) {
        String sql = """
                SELECT libraries.name
                FROM libraries
                JOIN book_libraries ON book_libraries.library_id = libraries.id
                WHERE book_libraries.book_id = ?
                ORDER BY libraries.id
                """;

        List<String> names = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("name"),
                book.getId()
        );
        book.setLibraryNames(names);
    }
}
