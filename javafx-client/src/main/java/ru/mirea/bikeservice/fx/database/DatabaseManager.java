package ru.mirea.bikeservice.fx.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager implements ConnectionProvider {
    private final DatabaseConfig config;

    public DatabaseManager(DatabaseConfig config) {
        this.config = config;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }

    public void testConnection() throws SQLException {
        try (Connection ignored = getConnection()) {
            // Успешное открытие и закрытие соединения является проверкой.
        }
    }
}
