package com.example.reservelib.catalog;

import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.catalog.dto.LibrariesBatchCheckResponse;
import com.example.reservelib.catalog.dto.LibraryCheckResponse;
import com.example.reservelib.catalog.dto.LibraryRefreshStatusResponse;
import com.example.reservelib.model.Book;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book Catalog", description = "Endpoints for managing and searching books in the catalog")
public class BookCatalogController {

    private final BookCatalogService bookCatalogService;

    public BookCatalogController(BookCatalogService bookCatalogService) {
        this.bookCatalogService = bookCatalogService;
    }

    @GetMapping
    @Operation(summary = "Get all books or search by title",
            description = "Returns a list of all books if no title is provided. " +
                          "If a title is provided, it performs a case-insensitive search for books with a matching title.")
    public List<Book> findBooks(
            @Parameter(description = "Title to search for (case-insensitive, partial match)")
            @RequestParam(required = false) String title
    ) {
        if (title != null && !title.isBlank()) {
            return bookCatalogService.searchBooksByTitle(title);
        }
        return bookCatalogService.getAllBooks();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookCatalogService.getBookById(id);
    }

    @GetMapping("/{id}/libraries/refresh-status")
    public LibraryRefreshStatusResponse getLibrariesRefreshStatus(@PathVariable Long id) {
        return bookCatalogService.getLibrariesRefreshStatus(id);
    }

    @PostMapping("/libraries/check-stale")
    public LibrariesBatchCheckResponse checkStaleLibrariesByIrbis() {
        return bookCatalogService.checkStaleLibraries();
    }

    @PostMapping("/{id}/libraries/refresh")
    public Map<String, String> refreshLibrariesByIrbis(@PathVariable Long id) {
        bookCatalogService.refreshLibrariesByBookId(id);
        return Map.of("message", "Libraries updated successfully");
    }

    @PostMapping("/{id}/libraries/check")
    public LibraryCheckResponse checkLibrariesByIrbis(@PathVariable Long id) {
        return bookCatalogService.checkLibrariesByBookId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book createBook(@Valid @RequestBody BookRequest request) {
        return bookCatalogService.createBook(request);
    }

    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return bookCatalogService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookCatalogService.deleteBook(id);
    }
}