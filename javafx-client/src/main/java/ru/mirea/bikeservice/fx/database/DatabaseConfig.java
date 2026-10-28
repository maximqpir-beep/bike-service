package ru.mirea.bikeservice.fx.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record DatabaseConfig(String url, String user, String password) {
    public static DatabaseConfig load() {
        Properties properties = new Properties();
        try (InputStream stream = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
            if (stream == null) throw new IllegalStateException("Ресурс database.properties не найден.");
            properties.load(stream);
        } catch (IOException error) {
            throw new IllegalStateException("Не удалось прочитать database.properties.", error);
        }

        return new DatabaseConfig(
                environmentOrProperty("DB_URL", properties, "db.url"),
                environmentOrProperty("DB_USER", properties, "db.user"),
                environmentOrProperty("DB_PASSWORD", properties, "db.password")
        );
    }

    private static String environmentOrProperty(String environment, Properties properties, String key) {
        String value = System.getenv(environment);
        return value == null || value.isBlank() ? properties.getProperty(key, "").trim() : value.trim();
    }
}
