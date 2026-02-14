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
                    .uri("https://www.googleapis.com/books/v1/volumes?q={title}&key=AIzaSyAvnA6FLGtyFkl72vEktwdQ5WgvGSCglz0", title)
                    .retrieve()
                    .body(String.class);
            System.out.println(json);
            JsonNode root = mapper.readTree(json);
            JsonNode items = root.get("items");
            System.out.println(items);

            if (items != null && !items.isEmpty()) {
                for (JsonNode item : items) {
                    JsonNode volumeInfo = item.get("volumeInfo");
                    JsonNode publisher = volumeInfo.get("publisher");
                    JsonNode description = volumeInfo.get("description");

                    if (publisher != null) {
                        String publisherText = publisher.asText().toLowerCase();

                        // Проверяем все варианты
                        if (publisherText.equals("литрес") ||
                                publisherText.contains("litres") ||
                                publisherText.contains("литрес")) {

                            if (description != null) {
                                return description.asText();
                            } else {
                                return "Описание отсутствует";
                            }
                        }
                    }
                }
                return "Книги издательства ЛитРес не найдены";
            }

            return "Книги не найдены";

        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }
}