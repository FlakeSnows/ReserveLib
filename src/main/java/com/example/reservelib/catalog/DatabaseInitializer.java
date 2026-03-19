package com.example.reservelib.catalog;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initializeDatabase();
    }

    private void initializeDatabase() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(200),
                    author VARCHAR(100),
                    isbn VARCHAR(20),
                    description VARCHAR(1000),
                    library_name VARCHAR(200)
                )
                """);

        try {
            jdbcTemplate.execute("""
                    DO $$
                    BEGIN
                        IF EXISTS (
                            SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'books'
                              AND column_name = 'isbn'
                        ) AND NOT EXISTS (
                            SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'books'
                              AND column_name = 'id'
                        ) THEN
                            ALTER TABLE books RENAME COLUMN isbn TO id;
                        END IF;
                    END $$;
                    """);
        } catch (DataAccessException ignored) {
            // H2 in tests does not support this PostgreSQL-specific block.
        }

        jdbcTemplate.execute("ALTER TABLE books ADD COLUMN IF NOT EXISTS isbn VARCHAR(20)");
    }
}
