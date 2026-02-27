package com.example.reservelib;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class YandexClient {
    private final RestClient restClient;
    private final YandexProperties properties;


    public YandexClient(YandexProperties properties) {
        this.restClient = RestClient.create();
        this.properties = properties;
    }


    public String getYandexResponse(String addres) {
        String baseUrl = properties.getBaseUrl();
        String apiKey = properties.getApiKey();
        return restClient.get()
                .uri(baseUrl, uriBuilder ->
                        uriBuilder
                                .queryParam("apikey" = apiKey))
                .queryPara
    }
}
