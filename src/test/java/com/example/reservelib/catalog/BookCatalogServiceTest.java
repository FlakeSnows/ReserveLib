package com.example.reservelib.catalog;

import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.catalog.dto.PageResponse;
import com.example.reservelib.model.Book;
import com.example.reservelib.irbis.IrbisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookCatalogServiceTest {

    private BookRepository bookRepository;
    private IrbisService irbisService;
    private BookCatalogService service;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        irbisService = mock(IrbisService.class);
        service = new BookCatalogService(bookRepository, irbisService);
    }

    @Test
    void testGetAllBooksEmpty() {
        // Если в базе пусто, должен вернуться пустой список
        when(bookRepository.countBooks(any(), any(), any())).thenReturn(0L);
        when(bookRepository.findBooks(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        PageResponse<Book> result = service.getAllBooks(0, 20);
        assertTrue(result.getContent().isEmpty(), "Список должен быть пустым");
    }

    @Test
    void testGetBookByIdNotFound() {
        // Если книги нет, должна быть ошибка 404 (ResponseStatusException)
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            service.getBookById(1L);
        }, "Должна быть ошибка, если книга не найдена");
    }

    @Test
    void testValidateRequestFail() {
        // Если передали пустую книгу, создание должно упасть
        BookRequest request = new BookRequest();
        request.setTitle(""); // Пустое название

        assertThrows(ResponseStatusException.class, () -> {
            service.createBook(request);
        });
    }

    @Test
    void testDeleteBookNotFound() {
        // Если удаляем то, чего нет, BookRepository вернет 0 измененных строк
        when(bookRepository.delete(anyLong())).thenReturn(0);

        assertThrows(ResponseStatusException.class, () -> {
            service.deleteBook(999L);
        });
    }
}