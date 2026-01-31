#  LibReserve

## 1. Регистрация пользователя
#### Method: POST
#### URI: /auth/register
#### Описание: Создание нового аккаунта пользователя
### Тело запроса:

### Заголовок запроса:
```
Accept:text/html,application/xhtml+xml
Accept-Encoding: gzip
Connection: keep-alive
Host: libreserve.com
User-Agent:Chrome/120.0.0.0 Safari/537.36
```
### Тело запроса:
```
{
  "email": "example@gmail.com",
  "password": "Pass1234",
  "name": "Иван Иванов",
  "phone": "+123456789"
}
```
### Коды ответов:

###  201 Created - успешная регистрация
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 256
Content-Type: application/json; charset=UTF-8
Server: nginx
```
### Тело ответа
```
{
  "email": "example@gmail.com",
  "name": "Иван Иванов",
}
```

###  400 Bad Request - некорректные данные
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 64
Content-Type: application/json; charset=UTF-8
Server: nginx
```
### Тело ответа
```
  {"error": "Email уже используется"}
```

###  422 Unprocessable Entity - ошибки валидации
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 64
Content-Type: application/json; charset=UTF-8
Server: nginx
```
### Тело ответа
```
  {"error":  "Пароль должен содержать минимум 8 символов"}
```


## 2. Авторизация
#### Method: POST
#### URI: /auth/login
#### Описание: Аутентификация пользователя и получение токена
### Заголовок запроса:
```
Accept:text/html,application/xhtml+xml
Accept-Encoding: gzip
Connection: keep-alive
Host: libreserve.com
User-Agent:Chrome/120.0.0.0 Safari/537.36
```
### Тело запроса:
```
{
  "email": "example@gmail.com",
  "password": "Pass1234"
}
```
### Коды ответов:

###  200 OK - успешный вход
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 256
Content-Type: application/json; charset=UTF-8
Server: nginx
```
#### Тело ответа
```
{
  "user": {
    "email": "example@gmail.com",
    "name": "Иван Иванов"
  }
}
```

###  401 Unauthorized - неверные учетные данные
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 64
Content-Type: application/json; charset=UTF-8
Server: nginx
```
#### Тело ответа
```
{"error": "Неверный email или пароль"}
```

## 3. Показ доступных книг
#### Method: GET
#### URI: /books
#### Описание: Получение списка книг с возможностью фильтрации
### Заголовок запроса:
```
Accept:text/html,application/xhtml+xml
Accept-Encoding: gzip
Connection: keep-alive
Host: libreserve.com
User-Agent:Chrome/120.0.0.0 Safari/537.36
```
### Коды ответов:

###  200 OK - успешное получение списка
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 64
Content-Type: application/json; charset=UTF-8
Server: nginx
```
#### Тело ответа
```
{
  "books": [
    {
      "id": "book-123",
      "title": "Мастер и Маргарита",
      "author": "Михаил Булгаков",
      "genre": "Роман",
      "available": true
    }
  ]
}
```

###  204 No Content - книги не найдены
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 64
Content-Type: application/json; charset=UTF-8
Server: nginx
```
#### Тело ответа
```
{"error":  "Книги не найдены"}
```

## 4. Рекомендации книг
#### Method: GET
#### URI: /api/recommendations
#### Описание: Получение рекомендаций книг
### Коды ответов:
###  200 OK - успешное получение рекомендаций
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 64
Content-Type: application/json; charset=UTF-8
Server: nginx
```
#### Тело ответа
```
{
  "recommendations": [
    {
      "id": "book-456",
      "title": "Преступление и наказание",
      "author": "Фёдор Достоевский",
      "genre": "Роман",
    }
  ]
}
```

### 401 Unauthorized - требуется авторизация
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 64
Content-Type: application/json; charset=UTF-8
Server: nginx
```
#### Тело ответа
```
{"error": "Требуется авторизация"}
```

###  404 Not Found - недостаточно данных для рекомендаций
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 64
Content-Type: application/json; charset=UTF-8
Server: nginx
```
#### Тело ответа
```
{"message": "Нету рекомендаций"}
```

## 5. Карта доступных библиотек
#### Method: GET
#### URI: /api/libraries
#### Описание: Получение информации о библиотеках
### Коды ответов:

###  200 OK - успешное получение списка библиотек
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 64
Content-Type: application/json; charset=UTF-8
Server: nginx
```
#### Тело ответа
```
{
  "libraries": [
    {
      "id": "lib-789",
      "name": "Центральная библиотека",
      "address": "ул. Ленина, 15",
      "availableBooks": 124
    }
  ]
}
```

### 204 No Content - библиотеки не найдены 
#### Заголовок ответа
```
Content-Encoding: gzip
Content-Length: 64
Content-Type: application/json; charset=UTF-8
Server: nginx
```
#### Тело ответа
```
{"error":  "Библиотеки не найдены"}
```
