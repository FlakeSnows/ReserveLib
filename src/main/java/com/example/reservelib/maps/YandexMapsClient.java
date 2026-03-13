package com.example.reservelib.maps;

import com.example.reservelib.dto.YandexGeocodeResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class YandexMapsClient {

    private final RestClient restClient;
    private final YandexMapsProperties properties;

    public YandexMapsClient(YandexMapsProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    public YandexGeocodeResponse getResponse(String address) {
        String uri = UriComponentsBuilder
                .fromUriString(properties.getProvider().getBaseUrl())
                .queryParam("apikey", properties.getProvider().getApiKey())
                .queryParam("geocode", address)
                .queryParam("format", "json")
                .queryParam("results", 1)
                .build()
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(YandexGeocodeResponse.class);
    }
}
