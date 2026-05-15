package com.example.reservelib.books;

import com.example.reservelib.books.dto.BookDto;
import com.example.reservelib.books.dto.BooksResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BooksService {
    private final BooksClient client;
    private final BooksProperties properties;

    public BooksService(BooksClient client, BooksProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public List<BookDto> getDescription(String title) {
        List<BookDto> bookInfo = new ArrayList<>();
        try {
            BooksResponse response = client.getResponse(title);

            if (response == null || response.getItems() == null) {
                return bookInfo;
            }

            for (BooksResponse.Item item : response.getItems()) {
                if (item.getVolumeInfo() == null) {
                    continue;
                }

                String publisher = item.getVolumeInfo().getPublisher();
                if (publisher == null) {
                    publisher = "Неизвестный издатель";
                }

                String description = item.getVolumeInfo().getDescription();
                if (description == null) {
                    description = "Нет описания";
                }

                String bookTitle = item.getVolumeInfo().getTitle();
                if (bookTitle == null) {
                    bookTitle = "Без названия";
                }

                List<String> authors = item.getVolumeInfo().getAuthors();
                if (authors == null) {
                    authors = new ArrayList<>();
                }

                bookInfo.add(new BookDto(description, authors, publisher, bookTitle));
            }

        } catch (Exception e) {
            System.out.println("Ошибка при поиске книги: " + e.getMessage());
        }
        return bookInfo;
    }
}