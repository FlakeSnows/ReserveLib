package com.example.reservelib.catalog;

import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.model.Book;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookCatalogController {

    private final BookCatalogService bookCatalogService;

    public BookCatalogController(BookCatalogService bookCatalogService) {
        this.bookCatalogService = bookCatalogService;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return bookCatalogService.getAllBooks();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable int id) {
        return bookCatalogService.getBookById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book createBook(@RequestBody BookRequest request) {
        return bookCatalogService.createBook(request);
    }

    @PutMapping("/{id}")
    public Book updateBook(@PathVariable int id, @RequestBody BookRequest request) {
        return bookCatalogService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable int id) {
        bookCatalogService.deleteBook(id);
    }
}
