/*
package com.example.reservelib;

import com.example.reservelib.dto.BookDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class YandexController {


    private final BooksService booksService;


    public YandexController(YandexProperties properties, BooksService booksService) {

        this.booksService = booksService;
    }

    @GetMapping("/description")
    public List<BookDto> getDescription(@RequestParam String title) {
        return booksService.getDescription(title);
    }
}*/
