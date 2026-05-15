package com.example.reservelib.catalog.dto;

import java.util.List;

public class LibraryCheckResponse {

    private Long bookId;
    private String title;
    private String lastLibrariesSyncAt;
    private int librariesCount;
    private List<String> libraries;

    public LibraryCheckResponse() {
    }

    public LibraryCheckResponse(Long bookId, String title, String lastLibrariesSyncAt, int librariesCount, List<String> libraries) {
        this.bookId = bookId;
        this.title = title;
        this.lastLibrariesSyncAt = lastLibrariesSyncAt;
        this.librariesCount = librariesCount;
        this.libraries = libraries;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastLibrariesSyncAt() {
        return lastLibrariesSyncAt;
    }

    public void setLastLibrariesSyncAt(String lastLibrariesSyncAt) {
        this.lastLibrariesSyncAt = lastLibrariesSyncAt;
    }

    public int getLibrariesCount() {
        return librariesCount;
    }

    public void setLibrariesCount(int librariesCount) {
        this.librariesCount = librariesCount;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }
}
