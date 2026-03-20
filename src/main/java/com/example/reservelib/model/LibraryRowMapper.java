package com.example.reservelib.model;

import com.example.reservelib.catalog.dto.LibraryResponse;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LibraryRowMapper implements RowMapper<LibraryResponse> {

    @Override
    public LibraryResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LibraryResponse(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("address"),
                (Double) rs.getObject("latitude"),
                (Double) rs.getObject("longitude")
        );
    }
}
