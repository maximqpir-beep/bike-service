package ru.mirea.bikeservice.fx.dao.jdbc;

import ru.mirea.bikeservice.fx.dao.ClientDao;
import ru.mirea.bikeservice.fx.database.ConnectionProvider;
import ru.mirea.bikeservice.fx.model.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcClientDao extends AbstractJdbcDao implements ClientDao {
    public JdbcClientDao(ConnectionProvider connections) { super(connections); }

    @Override
    public List<Client> findAll() {
        String sql = "SELECT id, full_name, phone, email FROM clients ORDER BY full_name, id";
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Client> clients = new ArrayList<>();
            while (resultSet.next()) clients.add(mapRow(resultSet));
            return clients;
        } catch (SQLException error) {
            throw databaseError("Не удалось загрузить клиентов", error);
        }
    }

    @Override
    public Optional<Client> findById(long id) {
        String sql = "SELECT id, full_name, phone, email FROM clients WHERE id = ?";
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException error) {
            throw databaseError("Не удалось получить клиента", error);
        }
    }

    private Client mapRow(ResultSet row) throws SQLException {
        return new Client(
                row.getLong("id"),
                row.getString("full_name"),
                row.getString("phone"),
                row.getString("email")
        );
    }
}
