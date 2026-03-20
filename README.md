# ReserveLib
Приложение для поиска книг и просмотра библиотек Екатеринбурга, в которых эти книги доступны.

## Требования

- Java 21+
- Maven 3.9+
- PostgreSQL

## База данных

Нужно создать базу данных `library` в PostgreSQL.

Пример параметров подключения:
- host: `localhost`
- port: `2593`
- database: `library`
- username: `postgres`
- password: задается в `application.properties` или через переменные окружения

## Конфигурация и секреты

Основные параметры можно задавать в `src/main/resources/application.properties` или через переменные окружения:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `BOOKS_API_KEY`
- `YANDEX_MAPS_API_KEY`


## 7. Полезные URL
```md
## Полезные URL

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Healthcheck: `http://localhost:8080/actuator/health`
```
## Основные API endpoints

### Книги
- `GET /api/books` — получить список книг
- `GET /api/books/{id}` — получить книгу по id
- `POST /api/books` — создать книгу
- `PUT /api/books/{id}` — обновить книгу
- `DELETE /api/books/{id}` — удалить книгу
- `GET /api/books/{id}/libraries` — получить библиотеки, в которых есть книга

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

