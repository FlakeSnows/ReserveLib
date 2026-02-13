package com.example.TEST;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/books")
public class BooksController {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/description")
    public String getDescription(@RequestParam String title) {
        try {
            String json = restClient.get()
                    .uri("https://www.googleapis.com/books/v1/volumes?q={title}", title)
                    .retrieve()
                    .body(String.class);
            System.out.println(json);
            JsonNode root = mapper.readTree(json);
            JsonNode items = root.get("items");

            if (items != null && !items.isEmpty()) {
                JsonNode volumeInfo = items.get(0).get("volumeInfo");
                JsonNode description = volumeInfo.get("description");

                if (description != null) {
                    return description.asText();
                } else {
                    return "Описание отсутствует";
                }
            }

            return "Книга не найдена";

        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }
}