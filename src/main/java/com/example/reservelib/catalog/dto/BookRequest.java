package com.example.reservelib.catalog.dto;

import java.util.List;

public class BookRequest {

    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String description;
    private List<String> libraryNames;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLibraryNames() {
        return libraryNames;
    }

    public void setLibraryNames(List<String> libraryNames) {
        this.libraryNames = libraryNames;
    }
}
