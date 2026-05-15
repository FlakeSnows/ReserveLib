package com.example.reservelib.catalog.dto;

public class LibraryRefreshStatusResponse {

    private Long bookId;
    private boolean stale;
    private String lastLibrariesSyncAt;

    public LibraryRefreshStatusResponse() {
    }

    public LibraryRefreshStatusResponse(Long bookId, boolean stale, String lastLibrariesSyncAt) {
        this.bookId = bookId;
        this.stale = stale;
        this.lastLibrariesSyncAt = lastLibrariesSyncAt;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public boolean isStale() {
        return stale;
    }

    public void setStale(boolean stale) {
        this.stale = stale;
    }

    public String getLastLibrariesSyncAt() {
        return lastLibrariesSyncAt;
    }

    public void setLastLibrariesSyncAt(String lastLibrariesSyncAt) {
        this.lastLibrariesSyncAt = lastLibrariesSyncAt;
    }
}
