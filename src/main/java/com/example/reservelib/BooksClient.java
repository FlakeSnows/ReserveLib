package com.example.reservelib;

import com.example.reservelib.dto.BooksResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BooksClient {

    private final RestClient restClient;

    public BooksClient() {
        this.restClient = RestClient.create();
    }

    public BooksResponse getResponse(String baseUrl, String title, String apiKey) {
        return restClient.get()
                .uri(baseUrl + "?q=" + title + "&key=" + apiKey)
                .retrieve()
                .body(BooksResponse.class);
    }


}
