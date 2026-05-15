package com.example.reservelib.catalog;

import com.example.reservelib.catalog.dto.BookRequest;
import com.example.reservelib.catalog.dto.LibrariesBatchCheckResponse;
import com.example.reservelib.catalog.dto.LibraryCheckFailure;
import com.example.reservelib.catalog.dto.LibraryCheckResponse;
import com.example.reservelib.catalog.dto.LibraryRefreshStatusResponse;
import com.example.reservelib.catalog.dto.PageResponse;
import com.example.reservelib.irbis.IrbisService;
import com.example.reservelib.irbis.dto.IrbisLibrariesResponse;
import com.example.reservelib.model.Book;
import com.example.reservelib.util.DateTimeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BookCatalogService {
    private static final Logger log = LoggerFactory.getLogger(BookCatalogService.class);
    private static final long LIBRARIES_SYNC_TTL_DAYS = 5L;

    private final BookRepository bookRepository;
    private final IrbisService irbisService;

    public BookCatalogService(BookRepository bookRepository, IrbisService irbisService) {
        this.bookRepository = bookRepository;
        this.irbisService = irbisService;
    }

    public PageResponse<Book> searchBooks(String title, String author, String genre, int page, int size) {
        log.debug("Searching books with title: '{}', author: '{}', genre: '{}', page: {}, size: {}", title, author, genre, page, size);
        long totalElements = bookRepository.countBooks(title, author, genre);
        List<Book> books = bookRepository.findBooks(title, author, genre, page, size);
        bookRepository.fillLibraryNames(books);
        
        return new PageResponse<>(books, page, size, totalElements);
    }

    public PageResponse<Book> getAllBooks(int page, int size) {
        return searchBooks(null, null, null, page, size);
    }

    public Book getBookById(Long id) {
        log.debug("Fetching book by id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book with id {} not found", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
                });
        bookRepository.fillLibraryNames(List.of(book));
        return book;
    }

    @Transactional
    public Book createBook(BookRequest request) {
        log.info("Creating new book: '{}' by '{}'", request.getTitle(), request.getAuthor());
        validateRequest(request);
        Long id = bookRepository.create(request);
        syncBookLibrariesByNames(id, request.getLibraryNames());
        log.info("Successfully created book with id: {}", id);
        return getBookById(id);
    }

    @Transactional
    public Book updateBook(Long id, BookRequest request) {
        log.info("Updating book with id: {}", id);
        ensureBookExists(id);
        validateRequest(request);
        bookRepository.update(id, request);
        syncBookLibrariesByNames(id, request.getLibraryNames());
        log.info("Successfully updated book with id: {}", id);
        return getBookById(id);
    }

    public void deleteBook(Long id) {
        log.info("Attempting to delete book with id: {}", id);
        int updated = bookRepository.delete(id);
        if (updated == 0) {
            log.warn("Failed to delete book with id: {} (not found)", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        log.info("Successfully deleted book with id: {}", id);
    }

    @Transactional
    public void refreshLibrariesByBookId(Long id) {
        log.info("Forcing IRBIS refresh for book id: {}", id);
        Book book = getBookById(id);
        IrbisLibrariesResponse response = irbisService.fetchLibrariesByTitle(book.getTitle());
        saveIrbisLibrariesForBook(id, response.libraries());
        log.info("Successfully refreshed libraries for book id: {}. Found {} libraries.", id, response.count());
    }

    public LibraryCheckResponse checkLibrariesByBookId(Long id) {
        log.debug("Checking libraries for book id: {}", id);
        Book book = getBookById(id);
        LocalDateTime lastSyncAt = book.getLastLibrariesSyncAt();
        IrbisLibrariesResponse libraries;

        if (isLibrariesSyncStale(lastSyncAt)) {
            log.info("Libraries for book id {} are stale or missing. Fetching from IRBIS...", id);
            libraries = irbisService.fetchLibrariesByTitle(book.getTitle());
            saveIrbisLibrariesForBook(id, libraries.libraries());
            lastSyncAt = bookRepository.getLastLibrariesSyncAt(id);
            log.info("Fetched and saved {} libraries from IRBIS for book id: {}", libraries.count(), id);
        } else {
            log.debug("Libraries for book id {} are up-to-date (last sync: {})", id, lastSyncAt);
            List<String> libraryNames = book.getLibraryNames().stream()
                    .sorted()
                    .toList();
            libraries = new IrbisLibrariesResponse(book.getTitle(), libraryNames.size(), libraryNames);
        }

        return new LibraryCheckResponse(
                id,
                book.getTitle(),
                DateTimeView.moscowHour(lastSyncAt),
                libraries.count(),
                libraries.libraries()
        );
    }

    public LibrariesBatchCheckResponse checkStaleLibraries() {
        log.info("Starting batch check for stale libraries");
        List<Long> bookIds = bookRepository.findStaleBookIds();
        log.info("Found {} books requiring library sync", bookIds.size());

        List<LibraryCheckResponse> results = new ArrayList<>();
        List<LibraryCheckFailure> failed = new ArrayList<>();
        for (Long bookId : bookIds) {
            try {
                results.add(checkLibrariesByBookId(bookId));
            } catch (Exception e) {
                log.error("Failed to check libraries for book id: {}", bookId, e);
                failed.add(new LibraryCheckFailure(bookId, e.getMessage()));
            }
        }

        log.info("Batch check completed. Success: {}, Failed: {}", results.size(), failed.size());
        return new LibrariesBatchCheckResponse(
                bookIds.size(),
                results.size(),
                failed.size(),
                results,
                failed
        );
    }

    public LibraryRefreshStatusResponse getLibrariesRefreshStatus(Long id) {
        ensureBookExists(id);
        LocalDateTime updatedAt = bookRepository.getLastLibrariesSyncAt(id);
        return new LibraryRefreshStatusResponse(id, isLibrariesSyncStale(updatedAt), DateTimeView.moscowHour(updatedAt));
    }

    private boolean isLibrariesSyncStale(LocalDateTime lastSyncAt) {
        return lastSyncAt == null || !lastSyncAt.isAfter(LocalDateTime.now().minusDays(LIBRARIES_SYNC_TTL_DAYS));
    }

    private void saveIrbisLibrariesForBook(Long bookId, List<String> libraries) {
        syncBookLibrariesByNames(bookId, libraries);
        bookRepository.updateLastLibrariesSyncAt(bookId);
    }

    private void ensureBookExists(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
    }

    @Transactional
    private void syncBookLibrariesByNames(Long bookId, List<String> libraryNames) {
        bookRepository.clearBookLibraries(bookId);
        for (String name : normalizeLibraryNames(libraryNames)) {
            bookRepository.insertLibraryIfNotExists(name);
            Long libraryId = bookRepository.findLibraryIdByName(name);
            bookRepository.linkBookAndLibrary(bookId, libraryId);
        }
    }

    private List<String> normalizeLibraryNames(List<String> names) {
        if (names == null) {
            return Collections.emptyList();
        }
        return names.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private void validateRequest(BookRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
    }
}