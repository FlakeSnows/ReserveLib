package com.example.reservelib.maps;

import com.example.reservelib.dto.CoordinatesDto;
import com.example.reservelib.dto.YandexGeocodeResponse;
import org.springframework.stereotype.Service;

@Service
public class YandexMapsService {

    private final YandexMapsClient client;

    public YandexMapsService(YandexMapsClient client) {
        this.client = client;
    }

    public CoordinatesDto getCoordinates(String address) {
        try {
            String pos = extractPosition(client.getResponse(address));

            if (pos == null) {
                return empty(address);
            }

            String[] coords = pos.trim().split(" ");

            return new CoordinatesDto(address, coords[1], coords[0]);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return empty(address);
        }
    }

    private String extractPosition(YandexGeocodeResponse response) {
        return response.getResponse().getGeoObjectCollection().getFeatureMember().getFirst().getGeoObject().getPoint().getPos();
    }

    private CoordinatesDto empty(String address) {
        return new CoordinatesDto(address, null, null);
    }
}
