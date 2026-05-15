# ReserveLib
Приложение для поиска книг и просмотра библиотек Екатеринбурга, в которых эти книги доступны.

## Требования

- Java 21+
- Maven 3.9+
- PostgreSQL

## База данных

При запуске через Docker Compose база данных настраивается автоматически.

Пример параметров подключения (в контейнере / локально):
- host: `lib_db` / `localhost`
- port: `5432`
- database: `lib_db`
- username: `user`
- password: `password`

## Запуск приложения

### Через Docker Compose (рекомендуется)
1. Убедитесь, что у вас установлен Docker и Docker Compose.
2. Соберите и запустите контейнеры:
   ```bash
   docker-compose up --build
   ```
   Приложение будет доступно по адресу `http://localhost:8081`.

### Локальный запуск (Maven)
1. Убедитесь, что у вас установлена Java 21 и Maven.
2. Запустите локальную БД PostgreSQL (или используйте Docker-контейнер `lib_db`).
3. Установите переменные окружения (или настройте `application.properties`):
   - `DB_URL=jdbc:postgresql://localhost:5432/lib_db`
   - `DB_USERNAME=user`
   - `DB_PASSWORD=password`
   - `BOOKS_API_KEY=ваш_ключ`
4. Запустите приложение:
   ```bash
   mvn spring-boot:run
   ```
   Приложение будет доступно по адресу `http://localhost:8081`.

## Конфигурация и секреты

Основные параметры можно задавать в `src/main/resources/application.properties` или через переменные окружения:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `BOOKS_API_KEY`


## 7. Полезные URL
```md

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Healthcheck: http://localhost:8080/actuator/health
```
## Основные API endpoints

### Книги
- `GET /api/books` — получить список книг
- `GET /api/books/{id}` — получить книгу по id
- `POST /api/books` — создать книгу
- `PUT /api/books/{id}` — обновить книгу
- `DELETE /api/books/{id}` — удалить книгу
- `GET /api/books/{id}/libraries` — получить библиотеки, в которых есть книга
- `POST /api/books/{id}/libraries/check` — проверить библиотеки через IRBIS, если последняя проверка была больше 5 дней назад
- `POST /api/books/libraries/check-stale` — проверить через IRBIS все книги, которые не проверялись 5 дней или больше

## Пример создания книги

```json
{
  "title": "Мастер и Маргарита",
  "author": "Михаил Булгаков",
  "isbn": "978-5-17-149175-4",
  "description": "Роман о добре и зле",
  "genre": "Детектив",
  "libraryNames": ["ЦГБ", "ГБИЦ"]
}
```


## Структура данных

Используются таблицы:
- `books`
- `libraries`
- `book_libraries`
