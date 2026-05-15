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
    private static final long LIBRARIES_SYNC_TTL_DAYS = 5L;

    private final BookRepository bookRepository;
    private final IrbisService irbisService;

    public BookCatalogService(BookRepository bookRepository, IrbisService irbisService) {
        this.bookRepository = bookRepository;
        this.irbisService = irbisService;
    }

    public PageResponse<Book> searchBooks(String title, String author, String genre, int page, int size) {
        long totalElements = bookRepository.countBooks(title, author, genre);
        List<Book> books = bookRepository.findBooks(title, author, genre, page, size);
        bookRepository.fillLibraryNames(books);
        
        return new PageResponse<>(books, page, size, totalElements);
    }

    public PageResponse<Book> getAllBooks(int page, int size) {
        return searchBooks(null, null, null, page, size);
    }

    public Book getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        bookRepository.fillLibraryNames(List.of(book));
        return book;
    }

    @Transactional
    public Book createBook(BookRequest request) {
        validateRequest(request);
        Long id = bookRepository.create(request);
        syncBookLibrariesByNames(id, request.getLibraryNames());
        return getBookById(id);
    }

    @Transactional
    public Book updateBook(Long id, BookRequest request) {
        ensureBookExists(id);
        validateRequest(request);
        bookRepository.update(id, request);
        syncBookLibrariesByNames(id, request.getLibraryNames());
        return getBookById(id);
    }

    public void deleteBook(Long id) {
        int updated = bookRepository.delete(id);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
    }

    @Transactional
    public void refreshLibrariesByBookId(Long id) {
        Book book = getBookById(id);
        IrbisLibrariesResponse response = irbisService.fetchLibrariesByTitle(book.getTitle());
        saveIrbisLibrariesForBook(id, response.libraries());
    }

    public LibraryCheckResponse checkLibrariesByBookId(Long id) {
        Book book = getBookById(id);
        LocalDateTime lastSyncAt = book.getLastLibrariesSyncAt();
        IrbisLibrariesResponse libraries;

        if (isLibrariesSyncStale(lastSyncAt)) {
            libraries = irbisService.fetchLibrariesByTitle(book.getTitle());
            saveIrbisLibrariesForBook(id, libraries.libraries());
            lastSyncAt = bookRepository.getLastLibrariesSyncAt(id);
        } else {
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
        List<Long> bookIds = bookRepository.findStaleBookIds();

        List<LibraryCheckResponse> results = new ArrayList<>();
        List<LibraryCheckFailure> failed = new ArrayList<>();
        for (Long bookId : bookIds) {
            try {
                results.add(checkLibrariesByBookId(bookId));
            } catch (Exception e) {
                failed.add(new LibraryCheckFailure(bookId, e.getMessage()));
            }
        }

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