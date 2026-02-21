package com.example.reservelib;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BooksController {

    private final BooksProperties properties;
    private final BooksService booksService;


    public BooksController(BooksProperties properties, BooksService booksService) {
        this.properties = properties;
        this.booksService = booksService;
    }

    @GetMapping("/description")
    public String getDescription(@RequestParam String title) {
        try {
            return booksService.getDescription(title);
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }
}