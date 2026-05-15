package com.example.reservelib.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.reservelib.util.DateTimeView;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Book {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String description;
    private LocalDateTime lastLibrariesSyncAt;
    private List<String> libraryNames = new ArrayList<>();

    public Book() {
    }

    public Book(Long id, String title, String author, String isbn, String genre, String description) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    @JsonIgnore
    public LocalDateTime getLastLibrariesSyncAt() {
        return lastLibrariesSyncAt;
    }

    public void setLastLibrariesSyncAt(LocalDateTime lastLibrariesSyncAt) {
        this.lastLibrariesSyncAt = lastLibrariesSyncAt;
    }

    @JsonProperty("lastLibrariesSyncAt")
    public String getLastLibrariesSyncAtView() {
        return DateTimeView.moscowHour(lastLibrariesSyncAt);
    }

    public List<String> getLibraryNames() {
        return libraryNames;
    }

    public void setLibraryNames(List<String> libraryNames) {
        this.libraryNames = libraryNames;
    }
}
