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
                CREATE TABLE IF NOT EXISTS libraries (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(200) NOT NULL UNIQUE,
                    address VARCHAR(300) NOT NULL,
                    latitude DOUBLE PRECISION,
                    longitude DOUBLE PRECISION
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(200),
                    author VARCHAR(100),
                    isbn VARCHAR(20),
                    genre VARCHAR(100),
                    description VARCHAR(1000)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS book_libraries (
                    book_id INTEGER NOT NULL,
                    library_id INTEGER NOT NULL,
                    PRIMARY KEY (book_id, library_id),
                    CONSTRAINT fk_book_libraries_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                    CONSTRAINT fk_book_libraries_library FOREIGN KEY (library_id) REFERENCES libraries(id) ON DELETE CASCADE
                )
                """);

        try {
            jdbcTemplate.execute("""
                    INSERT INTO libraries (name, address, latitude, longitude)
                    VALUES
                      ('ЦГБ', 'Екатеринбург, ул. Чапаева, д. 5', NULL, NULL),
                      ('ГБИЦ', 'Екатеринбург, ул. Антона Валека, д. 12', NULL, NULL),
                      ('3', 'Екатеринбург, ул. Отто Шмидта, 78', NULL, NULL),
                      ('4', 'Екатеринбург, ул. Академика Бардина, 19', NULL, NULL),
                      ('5', 'Екатеринбург, ул. Чапаева, 3', NULL, NULL),
                      ('6', 'Екатеринбург, ул. Белореченская, 26а', NULL, NULL),
                      ('7', 'Екатеринбург, ул. Фролова, 29', NULL, NULL),
                      ('11', 'Екатеринбург, ул. Техническая, 81', NULL, NULL),
                      ('13', 'Екатеринбург, Проспект Седова, 30', NULL, NULL),
                      ('14', 'Екатеринбург, ул. Свердлова, 25', NULL, NULL),
                      ('15', 'Екатеринбург, ул. Билимбаевская, 33', NULL, NULL),
                      ('17', 'Екатеринбург, Проспект Ленина, 70', NULL, NULL),
                      ('18', 'Екатеринбург, ул. Менделеева, 17', NULL, NULL),
                      ('19', 'Екатеринбург, ул. Малышева, 128', NULL, NULL),
                      ('20', 'Екатеринбург, ул. Новгородцевой, 17', NULL, NULL),
                      ('21', 'Екатеринбург, ул. 40-летия Комсомола, 10', NULL, NULL),
                      ('22', 'Екатеринбург, Переулок Переходный, 2а', NULL, NULL),
                      ('23', 'Екатеринбург, Короткий пер., 12', NULL, NULL),
                      ('24', 'Екатеринбург, ул. Бажова, 162', NULL, NULL),
                      ('25', 'Екатеринбург, ул. Лагерная, 1', NULL, NULL),
                      ('26', 'Екатеринбург, ул. Белинского, 163Б', NULL, NULL),
                      ('27', 'Екатеринбург, ул. Черняховского, 35', NULL, NULL),
                      ('28', 'Екатеринбург, ул. Ильича, 20', NULL, NULL),
                      ('29', 'Екатеринбург, ул. Грибоедова, 23', NULL, NULL),
                      ('30', 'Екатеринбург, ул. Колхозников, 52', NULL, NULL),
                      ('31', 'Екатеринбург, Ремесленный переулок, 7', NULL, NULL),
                      ('32', 'Екатеринбург, ул. Предельная, 10Б', NULL, NULL),
                      ('33', 'Екатеринбург, ул. Адмирала Ушакова, 22', NULL, NULL),
                      ('35', 'Екатеринбург, ул. Кировградская, 9', NULL, NULL),
                      ('36', 'Екатеринбург, Проспект Космонавтов, 73а', NULL, NULL),
                      ('37', 'Екатеринбург, ул. Баумана, 9', NULL, NULL),
                      ('38', 'Екатеринбург, ул. Ползунова, 28', NULL, NULL),
                      ('40', 'Екатеринбург, ул. Старых Большевиков, 18', NULL, NULL),
                      ('41', 'Екатеринбург, ул. Донбасская, 20', NULL, NULL),
                      ('42', 'Екатеринбург, ул. Шефская, 96', NULL, NULL)
                    ON CONFLICT (name) DO NOTHING
                    """);
        } catch (DataAccessException ignored) {
        }
    }
}
