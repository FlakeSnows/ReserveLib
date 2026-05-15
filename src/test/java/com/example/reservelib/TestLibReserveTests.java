package com.example.reservelib;

import com.example.reservelib.catalog.BookCatalogService;
import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true"
})
@Transactional
class TestLibReserveTests {

    @Autowired
    private BookCatalogService service;

	@Test
	void contextLoads() {
        // Проверка что всё запустилось
	}

    @Test
    void testCreateAndGetBook() {
        // Проверяем, что книга реально сохраняется в БД
        BookRequest request = new BookRequest();
        request.setTitle("Тестовая книга");
        request.setAuthor("Тестовый автор");

        Book saved = service.createBook(request);
        assertNotNull(saved.getId(), "ID должен быть");

        Book found = service.getBookById(saved.getId());
        assertEquals("Тестовая книга", found.getTitle());
    }
}
