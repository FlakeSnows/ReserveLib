package com.example.reservelib;

import com.example.reservelib.dto.BooksResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BooksService {
    private final BooksClient client;
    private final BooksProperties properties;

    public BooksService(BooksClient client, BooksProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public String getDescription(String title) {
        try {

            String apiKey = properties.getProvider().getApiKey();
            String baseUrl = properties.getProvider().getBaseUrl();
            List<String> publisherKeywords = properties.getSearch().getPublisherKeywords();

            BooksResponse response = client.getResponse(baseUrl, title, apiKey);

            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                return "Книги не найдены";
            }
            for (BooksResponse.Item item : response.getItems()) {
                String publisher = item.getVolumeInfo().getPublisher();
                String description = item.getVolumeInfo().getDescription();

                if (publisher == null) {
                    continue;
                }

                String publisherText = publisher.toLowerCase();

                if (publisherKeywords.contains(publisherText)) {

                    if (description != null) {
                        return description;
                    } else {
                        return "Описание отсутствует";
                    }
                }

            }
            return "Книги издательства не найдены";
        } catch (
                Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }
}



