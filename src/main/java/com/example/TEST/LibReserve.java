package com.example.TEST;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class LibReserve {

    public static void main(String[] args) {
        SpringApplication.run(LibReserve.class, args);
    }
}
//http://localhost:8080/api/books/description?title=
