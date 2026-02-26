package com.example.reservelib;


import com.example.reservelib.dto.BookDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public List<BookDto> getDescription(@RequestParam String title) {
            return booksService.getDescription(title);
    }
}