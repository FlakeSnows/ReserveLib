package com.example.reservelib.books;

import com.example.reservelib.dto.BookDto;
import com.example.reservelib.dto.BooksResponse;
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

            List<String> publisherKeywords = properties.getSearch().getPublisherKeywords();

            BooksResponse response = client.getResponse(title);

            for (BooksResponse.Item item : response.getItems()) {
                String publisher = item.getVolumeInfo().getPublisher();
                if (publisher == null) {
                    continue;
                }

                boolean matches = false;

                String publisherText = publisher.toLowerCase().trim();
                for (String keyword : publisherKeywords) {
                    String k = keyword.toLowerCase().trim();
                    if (publisherText.contains(k)) {
                        matches = true;
                        break;
                    }
                }

                if (!matches) continue;
                String description = item.getVolumeInfo().getDescription();
                if (description == null) {
                    description = "Нет описания";
                }
                    String bookTitle = item.getVolumeInfo().getTitle();
                    List<String> authors = item.getVolumeInfo().getAuthors();
                    bookInfo.add(new BookDto(description, authors, publisher, bookTitle));
                }


        } catch (
                Exception e) {
            System.out.println(e.getMessage());
        }
        return bookInfo;
    }
}



