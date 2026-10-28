package ru.mirea.bikeservice.fx.exception;

import java.sql.SQLException;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String operation, SQLException cause) {
        super(message(operation, cause), cause);
    }

    private static String message(String operation, SQLException cause) {
        String state = cause.getSQLState();
        if (state != null && state.startsWith("08")) {
            return "Нет соединения с PostgreSQL. Проверьте Docker и параметры подключения.";
        }
        if (state != null && state.startsWith("28")) {
            return "PostgreSQL отклонил имя пользователя или пароль.";
        }
        if ("23505".equals(state)) return "Такое уникальное значение уже существует.";
        if ("23503".equals(state)) return "Операция нарушает связь между клиентом и заявкой.";
        if ("42P01".equals(state)) return "Таблицы не найдены. Запустите SQL-скрипты КР №1.";
        return operation + ". Код SQL: " + (state == null ? "неизвестен" : state) + ".";
    }
}
