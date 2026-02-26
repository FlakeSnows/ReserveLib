package com.example.reservelib.dto;

import java.util.List;

public class BookDto {

    private String description;
    private List<String> authors;
    private String publisher;
    private String title;

    public BookDto(String description, List<String> authors, String publisher,  String title) {
        this.description = description;
        this.authors = authors;
        this.publisher = publisher;
        this.title = title;
    }

    public String getDescription() {return description;}
    public List<String> getAuthors() {return authors;}
    public String getPublisher() {return publisher;}
    public String getTitle() {return title;}
}
