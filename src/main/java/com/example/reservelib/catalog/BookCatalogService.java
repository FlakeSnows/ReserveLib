package com.example.reservelib.catalog;

import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.model.Book;
import com.example.reservelib.model.BookRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BookCatalogService {

    private final JdbcTemplate jdbcTemplate;
    private final BookRowMapper bookRowMapper = new BookRowMapper();

    public BookCatalogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Book> getAllBooks() {
        return jdbcTemplate.query("SELECT * FROM books ORDER BY id", bookRowMapper);
    }

    public Book getBookById(int id) {
        List<Book> books = jdbcTemplate.query("SELECT * FROM books WHERE id = ?", bookRowMapper, id);
        if (books.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Book not found");
        }
        return books.getFirst();
    }

    public Book createBook(BookRequest request) {
        Integer id = jdbcTemplate.queryForObject(
                "INSERT INTO books (title, author, isbn, description, library_name) VALUES (?, ?, ?, ?, ?) RETURNING id",
                Integer.class,
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getDescription(),
                request.getLibraryName()
        );
        return getBookById(id);
    }

    public Book updateBook(int id, BookRequest request) {
        int updated = jdbcTemplate.update(
                "UPDATE books SET title = ?, author = ?, isbn = ?, description = ?, library_name = ? WHERE id = ?",
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getDescription(),
                request.getLibraryName(),
                id
        );
        if (updated == 0) {
            throw new ResponseStatusException(NOT_FOUND, "Book not found");
        }
        return getBookById(id);
    }

    public void deleteBook(int id) {
        int deleted = jdbcTemplate.update("DELETE FROM books WHERE id = ?", id);
        if (deleted == 0) {
            throw new ResponseStatusException(NOT_FOUND, "Book not found");
        }
    }
}
