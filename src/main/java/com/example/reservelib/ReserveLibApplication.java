package com.example.reservelib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ReserveLibApplication {

    public static void main(String[] args) {

        SpringApplication.run(ReserveLibApplication.class, args);
    }
}

//http://localhost:8080/api/books/description?title=
//https://www.googleapis.com/books/v1/volumes?q=Ð²Ð¾Ð¹Ð½Ð°&key=AIzaSyAvnA6FLGtyFkl72vEktwdQ5WgvGSCglz0
//http://localhost:8080/api/yandex/coordinates?address=
//ÑÐ´ÐµÐ»Ð°ÑÑ ÑÐ°Ð±Ð»Ð¸ÑÑ Ñ Ð°Ð´ÑÐµÑÐ°Ð¼Ð¸ Ð±Ð¸Ð±Ð»Ð¸Ð¾ÑÐµÐº Ð¸ ÑÐ²ÑÐ·Ð°ÑÑ Ñ Ð´ÑÑÐ³Ð¾Ð¹ ÑÐ°Ð±Ð»Ð¸ÑÐµÐ¹
