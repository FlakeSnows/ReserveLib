package com.example.reservelib.catalog.dto;

public class LibraryCheckFailure {

    private Long bookId;
    private String error;

    public LibraryCheckFailure() {
    }

    public LibraryCheckFailure(Long bookId, String error) {
        this.bookId = bookId;
        this.error = error;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
