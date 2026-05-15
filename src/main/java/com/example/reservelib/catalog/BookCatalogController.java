package com.example.reservelib.catalog;

import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.catalog.dto.LibrariesBatchCheckResponse;
import com.example.reservelib.catalog.dto.LibraryCheckResponse;
import com.example.reservelib.catalog.dto.LibraryRefreshStatusResponse;
import com.example.reservelib.catalog.dto.PageResponse;
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

import java.util.Map;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Каталог книг", description = "Управление книгами в каталоге библиотеки")
public class BookCatalogController {

    private final BookCatalogService bookCatalogService;

    public BookCatalogController(BookCatalogService bookCatalogService) {
        this.bookCatalogService = bookCatalogService;
    }

    @GetMapping
    @Operation(summary = "Получить список книг (с пагинацией)",
            description = "Возвращает книги постранично. Можно использовать фильтры.")
    public PageResponse<Book> findBooks(
            @Parameter(description = "Название книги (частичное совпадение)")
            @RequestParam(required = false) String title,
            @Parameter(description = "Автор (частичное совпадение)")
            @RequestParam(required = false) String author,
            @Parameter(description = "Жанр (частичное совпадение)")
            @RequestParam(required = false) String genre,
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество книг на странице")
            @RequestParam(defaultValue = "20") int size
    ) {
        return bookCatalogService.searchBooks(title, author, genre, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить книгу по ID")
    public Book getBookById(
            @Parameter(description = "ID книги") @PathVariable Long id) {
        return bookCatalogService.getBookById(id);
    }

    @GetMapping("/{id}/libraries/refresh-status")
    @Operation(summary = "Проверить статус актуальности библиотек для книги",
            description = "Возвращает информацию о том, нужно ли обновить список библиотек, в которых есть книга.")
    public LibraryRefreshStatusResponse getLibrariesRefreshStatus(
            @Parameter(description = "ID книги") @PathVariable Long id) {
        return bookCatalogService.getLibrariesRefreshStatus(id);
    }

    @PostMapping("/libraries/check-stale")
    @Operation(summary = "Массовая проверка устаревших библиотек",
            description = "Проверяет наличие в ИРБИС всех книг, у которых данные о библиотеках устарели или отсутствуют.")
    public LibrariesBatchCheckResponse checkStaleLibrariesByIrbis() {
        return bookCatalogService.checkStaleLibraries();
    }

    @PostMapping("/{id}/libraries/refresh")
    @Operation(summary = "Принудительно обновить список библиотек для книги",
            description = "Запрашивает актуальные данные из ИРБИС и обновляет их в базе.")
    public Map<String, String> refreshLibrariesByIrbis(
            @Parameter(description = "ID книги") @PathVariable Long id) {
        bookCatalogService.refreshLibrariesByBookId(id);
        return Map.of("message", "Библиотеки успешно обновлены");
    }

    @PostMapping("/{id}/libraries/check")
    @Operation(summary = "Проверить наличие книги в библиотеках (ИРБИС)",
            description = "Возвращает актуальный список библиотек для книги. Если данные в базе устарели, они будут обновлены автоматически.")
    public LibraryCheckResponse checkLibrariesByIrbis(
            @Parameter(description = "ID книги") @PathVariable Long id) {
        return bookCatalogService.checkLibrariesByBookId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить новую книгу в каталог")
    public Book createBook(@Valid @RequestBody BookRequest request) {
        return bookCatalogService.createBook(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить информацию о книге")
    public Book updateBook(
            @Parameter(description = "ID книги") @PathVariable Long id, 
            @Valid @RequestBody BookRequest request) {
        return bookCatalogService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить книгу из каталога")
    public void deleteBook(
            @Parameter(description = "ID книги") @PathVariable Long id) {
        bookCatalogService.deleteBook(id);
    }
}