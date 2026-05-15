package com.example.reservelib.catalog;

import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.model.Book;
import com.example.reservelib.irbis.IrbisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookCatalogServiceTest {

    private JdbcTemplate jdbcTemplate;
    private IrbisService irbisService;
    private BookCatalogService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        irbisService = mock(IrbisService.class);
        service = new BookCatalogService(jdbcTemplate, irbisService);
    }

    @Test
    void testGetAllBooksEmpty() {
        // Если в базе пусто, должен вернуться пустой список
        when(jdbcTemplate.query(anyString(), any(com.example.reservelib.model.BookRowMapper.class)))
                .thenReturn(Collections.emptyList());

        List<Book> result = service.getAllBooks();
        assertTrue(result.isEmpty(), "Список должен быть пустым");
    }

    @Test
    void testGetBookByIdNotFound() {
        // Если книги нет, должна быть ошибка 404 (ResponseStatusException)
        when(jdbcTemplate.query(anyString(), any(com.example.reservelib.model.BookRowMapper.class), anyLong()))
                .thenReturn(Collections.emptyList());

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
        // Если удаляем то, чего нет, JdbcTemplate вернет 0 измененных строк
        when(jdbcTemplate.update(anyString(), anyLong())).thenReturn(0);

        assertThrows(ResponseStatusException.class, () -> {
            service.deleteBook(999L);
        });
    }
}
