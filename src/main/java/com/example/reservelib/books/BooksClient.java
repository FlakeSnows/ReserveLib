package com.example.reservelib.books;

import com.example.reservelib.books.dto.BooksResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BooksClient {

    private final RestClient restClient;
    private final BooksProperties properties;

    public BooksClient(BooksProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    public BooksResponse getResponse(String title) {
        String baseUrl = properties.getProvider().getBaseUrl();
        String apiKey = properties.getProvider().getApiKey();
        return restClient.get()
                .uri(baseUrl + "?q=" + title + "&key=" + apiKey)
                .retrieve()
                .body(BooksResponse.class);
    }


}
