package com.example.reservelib.maps;

import com.example.reservelib.dto.CoordinatesDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/yandex")
public class YandexMapsController {

    private final YandexMapsService yandexMapsService;

    public YandexMapsController(YandexMapsService yandexMapsService) {
        this.yandexMapsService = yandexMapsService;
    }

    @GetMapping("/coordinates")
    public CoordinatesDto getCoordinates(@RequestParam String address) {
        return yandexMapsService.getCoordinates(address);
    }
}
