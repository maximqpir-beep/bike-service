package ru.mirea.bikeservice.util;

import java.sql.*;
public class DatabaseManager implements ConnectionProvider {
    private final String url;
    private final String user;
    private final String password;
    public DatabaseManager(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }
    public static DatabaseManager fromEnvironment() {
        return new DatabaseManager(System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/bike_service"),
            System.getenv().getOrDefault("DB_USER", "postgres"), System.getenv().getOrDefault("DB_PASSWORD", ""));
    }
    @Override public Connection getConnection() throws SQLException { return DriverManager.getConnection(url, user, password); }
}
