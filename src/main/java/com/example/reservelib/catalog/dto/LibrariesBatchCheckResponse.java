package com.example.reservelib.catalog.dto;

import java.util.List;

public class LibrariesBatchCheckResponse {

    private int booksToCheckCount;
    private int checkedBooksCount;
    private int failedBooksCount;
    private List<LibraryCheckResponse> results;
    private List<LibraryCheckFailure> failed;

    public LibrariesBatchCheckResponse() {
    }

    public LibrariesBatchCheckResponse(int booksToCheckCount, int checkedBooksCount, int failedBooksCount, List<LibraryCheckResponse> results, List<LibraryCheckFailure> failed) {
        this.booksToCheckCount = booksToCheckCount;
        this.checkedBooksCount = checkedBooksCount;
        this.failedBooksCount = failedBooksCount;
        this.results = results;
        this.failed = failed;
    }

    public int getBooksToCheckCount() {
        return booksToCheckCount;
    }

    public void setBooksToCheckCount(int booksToCheckCount) {
        this.booksToCheckCount = booksToCheckCount;
    }

    public int getCheckedBooksCount() {
        return checkedBooksCount;
    }

    public void setCheckedBooksCount(int checkedBooksCount) {
        this.checkedBooksCount = checkedBooksCount;
    }

    public int getFailedBooksCount() {
        return failedBooksCount;
    }

    public void setFailedBooksCount(int failedBooksCount) {
        this.failedBooksCount = failedBooksCount;
    }

    public List<LibraryCheckResponse> getResults() {
        return results;
    }

    public void setResults(List<LibraryCheckResponse> results) {
        this.results = results;
    }

    public List<LibraryCheckFailure> getFailed() {
        return failed;
    }

    public void setFailed(List<LibraryCheckFailure> failed) {
        this.failed = failed;
    }
}
