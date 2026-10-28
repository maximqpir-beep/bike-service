package ru.mirea.bikeservice.fx.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import ru.mirea.bikeservice.fx.dao.jdbc.JdbcClientDao;
import ru.mirea.bikeservice.fx.dao.jdbc.JdbcServiceRequestDao;
import ru.mirea.bikeservice.fx.database.DatabaseConfig;
import ru.mirea.bikeservice.fx.database.DatabaseManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
class JdbcReadIntegrationTest {
    @Test
    void readsKr1TablesThroughJavaFxDao() {
        DatabaseConfig config = new DatabaseConfig(
                System.getenv("TEST_DB_URL"),
                System.getenv("TEST_DB_USER"),
                System.getenv("TEST_DB_PASSWORD")
        );
        DatabaseManager database = new DatabaseManager(config);

        assertEquals(5, new JdbcClientDao(database).findAll().size());
        assertEquals(12, new JdbcServiceRequestDao(database).findAll().size());
    }
}
