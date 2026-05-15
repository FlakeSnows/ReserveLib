package com.example.reservelib.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class BookRowMapper implements RowMapper<Book> {

    @Override
    public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
        Book book = new Book(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"),
                rs.getString("genre"),
                rs.getString("description")
        );
        Timestamp syncedAt = rs.getTimestamp("last_libraries_sync_at");
        if (syncedAt != null) {
            book.setLastLibrariesSyncAt(syncedAt.toLocalDateTime());
        }
        return book;
    }
}
