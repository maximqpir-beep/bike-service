package ru.mirea.bikeservice.fx.dao.jdbc;

import ru.mirea.bikeservice.fx.database.ConnectionProvider;
import ru.mirea.bikeservice.fx.exception.DatabaseException;

import java.sql.SQLException;

public abstract class AbstractJdbcDao {
    protected final ConnectionProvider connections;

    protected AbstractJdbcDao(ConnectionProvider connections) {
        this.connections = connections;
    }

    protected DatabaseException databaseError(String operation, SQLException error) {
        return new DatabaseException(operation, error);
    }
}
