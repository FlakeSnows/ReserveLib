package com.example.reservelib.model;

public class Book {

    private int id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String libraryName;

    public Book() {
    }

    public Book(int id, String title, String author, String isbn, String description, String libraryName) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.description = description;
        this.libraryName = libraryName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", description='" + description + '\'' +
                ", libraryName='" + libraryName + '\'' +
                '}';
    }
}
