package ru.mirea.bikeservice.exception;

import java.sql.SQLException;
public class DatabaseException extends RuntimeException {
    public DatabaseException(SQLException cause) { super(message(cause), cause); }
    private static String message(SQLException e) {
        String state = e.getSQLState();
        if (state != null && state.startsWith("08")) return "Нет соединения с БД. Проверьте сервер и DB_URL/DB_USER/DB_PASSWORD.";
        if (state != null && state.startsWith("28")) return "База данных отклонила логин или пароль.";
        if ("23505".equals(state)) return "Такой телефон или email уже существует.";
        if ("23503".equals(state)) return "Нарушена связь: клиент не существует или у него есть заявки.";
        if (state != null && state.startsWith("23")) return "Данные нарушают ограничения базы данных.";
        return "Ошибка SQL (код " + state + "). Проверьте структуру БД и SQL-скрипты.";
    }
}
