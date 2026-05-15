CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(100),
    isbn VARCHAR(20),
    genre VARCHAR(100),
    description VARCHAR(1000),
    last_libraries_sync_at TIMESTAMP
);

CREATE TABLE libraries (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(500) NOT NULL UNIQUE
);

CREATE TABLE book_libraries (
    book_id BIGINT NOT NULL,
    library_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, library_id),
    CONSTRAINT fk_book_libraries_book
        FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_book_libraries_library
        FOREIGN KEY (library_id) REFERENCES libraries(id) ON DELETE CASCADE
);