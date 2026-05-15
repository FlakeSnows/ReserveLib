# ReserveLib

Бэкенд-сервис для управления каталогом книг и интеграции с системой ИРБИС (Екатеринбург).

## Стек технологий
- **Java 21**, **Spring Boot 3**, **Maven 3.9+**
- **PostgreSQL 15+**, **Flyway** (миграции БД)
- **Docker**, **Docker Compose**
- **Swagger / OpenAPI 3** (документация API)
- **Spring Actuator** (Healthcheck)

## Конфигурация и управление секретами
Секреты (учетные данные БД, API-ключи) не хранятся в коде. Приложение получает их из переменных окружения.
При запуске через Docker Compose переменные пробрасываются из файла `.env` в корне проекта.

**Обязательные переменные:**
* `DB_URL`
* `DB_USERNAME`
* `DB_PASSWORD`
* `BOOKS_API_KEY`

## Запуск проекта

**В Docker-контейнере (со сборкой образа и запуском БД):**
```bash
docker-compose up -d --build
```

**Локально средствами Java (БД должна быть запущена отдельно):**
```bash
./mvnw spring-boot:run
```

## База данных и миграции
- Используется PostgreSQL. Для персистентности данных в Docker настроен volume.
- Схема БД инициализируется автоматически при старте приложения с помощью **Flyway** (`src/main/resources/db/migration`).
- Структура включает таблицы `books`, `libraries` и `book_libraries` с соблюдением внешних ключей (Foreign Keys).

## API и Контракты
REST API использует корректные HTTP-методы (GET, POST, PUT, DELETE) и статусы ответов (200, 201, 204, 400, 404, 500). Включена валидация входных данных (Bean Validation) с возвратом структурированных ошибок 400 Bad Request.

* **Swagger UI:** http://localhost:8081/swagger-ui/index.html

**Ключевые эндпоинты:**
* `GET /api/books?page=0&size=20` — получение списка книг (с пагинацией и фильтрами).
* `POST /api/books` — создание книги (201 Created).
* `PUT /api/books/{id}` — обновление данных книги.
* `DELETE /api/books/{id}` — удаление книги (204 No Content).
* `POST /api/books/{id}/libraries/check` — проверка наличия в библиотеках через интеграцию с ИРБИС.

## Наблюдаемость (Observability)
* **Healthcheck:** http://localhost:8081/actuator/health (отражает статус сервиса и подключения к БД).
* **Логирование:** уровень логирования конфигурируется через `application.properties`. Логи включают timestamp, уровень, поток и сообщение.