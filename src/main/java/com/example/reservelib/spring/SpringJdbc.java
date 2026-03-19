package com.example.reservelib.spring;

import com.example.reservelib.model.Book;
import com.example.reservelib.model.BookRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

public class SpringJdbc {

    private static final DriverManagerDataSource dataSource = new DriverManagerDataSource();
    private static final JdbcTemplate jdbcTemplate;
    private static final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    static {
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/library"));
        dataSource.setUsername(System.getenv().getOrDefault("DB_USERNAME", "postgres"));
        dataSource.setPassword(System.getenv().getOrDefault("DB_PASSWORD", "postgres"));
        jdbcTemplate = new JdbcTemplate(dataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public static void main(String[] args) {
        initializeDatabase();
        checkTableExists();
    }

    public static void initializeDatabase() {
        String sql = """
                CREATE TABLE IF NOT EXISTS books (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(200) NOT NULL,
                    author VARCHAR(100) NOT NULL,
                    isbn VARCHAR(20),
                    description VARCHAR(1000),
                    library_name VARCHAR(200)
                )
                """;
        jdbcTemplate.execute(sql);
        System.out.println("Table books is ready");
    }

    public static void checkTableExists() {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'books'";
        List<String> tables = jdbcTemplate.queryForList(sql, String.class);
        System.out.println(tables.isEmpty() ? "Table books not found" : "Table books exists");
    }

    public static void addBook(String title, String author, String description, String libraryName) {
        String sql = "INSERT INTO books (title, author, description, library_name) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, title, author, description, libraryName);
    }

    public static void alternativeAddBook(String title, String author, String description, String libraryName) {
        var queryParams = new MapSqlParameterSource();
        queryParams.addValue("title", title)
                .addValue("author", author)
                .addValue("description", description)
                .addValue("libraryName", libraryName);
        String sql = "INSERT INTO books (title, author, description, library_name) VALUES (:title, :author, :description, :libraryName)";
        namedParameterJdbcTemplate.update(sql, queryParams);
    }

    public static List<Book> getAllBooks() {
        return jdbcTemplate.query("SELECT * FROM books ORDER BY id", new BookRowMapper());
    }
}
