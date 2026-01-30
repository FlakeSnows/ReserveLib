<img width="550" height="261" alt="image" src="https://github.com/user-attachments/assets/1b0165a4-2108-4799-88e4-22fc05a6e59e" /># Project

1. Регистрация пользователя
Method: POST
URI: /auth/register
Описание: Создание нового аккаунта пользователя
Заголовки запроса: Content-Type: application/json
Тело запроса:

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "name": "Иван Иванов",
  "phone": "+79001234567"
}
Коды ответов:

201 Created - успешная регистрация

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "Иван Иванов",
  "message": "Регистрация успешна"
}
400 Bad Request - некорректные данные

{
  "error": "Email уже используется",
  "code": "EMAIL_EXISTS"
}
422 Unprocessable Entity - ошибки валидации

{
  "errors": {
    "password": "Пароль должен содержать минимум 8 символов"
  }
}
2. Авторизация
Method: POST
URI: /auth/login
Описание: Аутентификация пользователя и получение токена
Заголовки запроса: Content-Type: application/json
Тело запроса:
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
Коды ответов:

200 OK - успешный вход

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "name": "Иван Иванов"
  }
}
401 Unauthorized - неверные учетные данные

{
  "error": "Неверный email или пароль"
}
