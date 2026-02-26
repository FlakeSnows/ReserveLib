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
//https://www.googleapis.com/books/v1/volumes?q=война&key=AIzaSyAvnA6FLGtyFkl72vEktwdQ5WgvGSCglz0