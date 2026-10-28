package ru.mirea.bikeservice.fx.dao.jdbc;

import ru.mirea.bikeservice.fx.dao.ServiceRequestDao;
import ru.mirea.bikeservice.fx.database.ConnectionProvider;
import ru.mirea.bikeservice.fx.model.BikeType;
import ru.mirea.bikeservice.fx.model.RequestStatus;
import ru.mirea.bikeservice.fx.model.ServiceRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcServiceRequestDao extends AbstractJdbcDao implements ServiceRequestDao {
    private static final String COLUMNS = "client_id, bike_brand, bike_model, bike_type, " +
            "problem_description, status, estimated_cost, created_at, completed_at";

    public JdbcServiceRequestDao(ConnectionProvider connections) { super(connections); }

    @Override
    public List<ServiceRequest> findAll() {
        String sql = "SELECT id, " + COLUMNS + " FROM service_requests ORDER BY id";
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<ServiceRequest> requests = new ArrayList<>();
            while (resultSet.next()) requests.add(mapRow(resultSet));
            return requests;
        } catch (SQLException error) {
            throw databaseError("Не удалось загрузить заявки", error);
        }
    }

    @Override
    public Optional<ServiceRequest> findById(long id) {
        String sql = "SELECT id, " + COLUMNS + " FROM service_requests WHERE id = ?";
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException error) {
            throw databaseError("Не удалось получить заявку", error);
        }
    }

    @Override
    public ServiceRequest insert(ServiceRequest request) {
        String sql = "INSERT INTO service_requests (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, request);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("PostgreSQL не вернул ID заявки.");
                request.setId(keys.getLong(1));
            }
            return request;
        } catch (SQLException error) {
            throw databaseError("Не удалось создать заявку", error);
        }
    }

    @Override
    public boolean update(ServiceRequest request) {
        String sql = "UPDATE service_requests SET client_id=?, bike_brand=?, bike_model=?, bike_type=?, " +
                "problem_description=?, status=?, estimated_cost=?, created_at=?, completed_at=? WHERE id=?";
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, request);
            statement.setLong(10, request.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException error) {
            throw databaseError("Не удалось изменить заявку", error);
        }
    }

    @Override
    public boolean deleteById(long id) {
        String sql = "DELETE FROM service_requests WHERE id = ?";
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException error) {
            throw databaseError("Не удалось удалить заявку", error);
        }
    }

    private void bind(PreparedStatement statement, ServiceRequest request) throws SQLException {
        statement.setLong(1, request.getClientId());
        statement.setString(2, request.getBikeBrand());
        statement.setString(3, blankToNull(request.getBikeModel()));
        statement.setString(4, request.getBikeType().name());
        statement.setString(5, request.getProblemDescription());
        statement.setString(6, request.getStatus().name());
        statement.setBigDecimal(7, request.getEstimatedCost());
        statement.setTimestamp(8, Timestamp.valueOf(request.getCreatedAt()));
        statement.setTimestamp(9, request.getCompletedAt() == null ? null : Timestamp.valueOf(request.getCompletedAt()));
    }

    private ServiceRequest mapRow(ResultSet row) throws SQLException {
        ServiceRequest request = new ServiceRequest();
        request.setId(row.getLong("id"));
        request.setClientId(row.getLong("client_id"));
        request.setBikeBrand(row.getString("bike_brand"));
        request.setBikeModel(row.getString("bike_model"));
        request.setBikeType(BikeType.valueOf(row.getString("bike_type")));
        request.setProblemDescription(row.getString("problem_description"));
        request.setStatus(RequestStatus.valueOf(row.getString("status")));
        request.setEstimatedCost(row.getBigDecimal("estimated_cost"));
        request.setCreatedAt(row.getTimestamp("created_at").toLocalDateTime());
        Timestamp completed = row.getTimestamp("completed_at");
        request.setCompletedAt(completed == null ? null : completed.toLocalDateTime());
        return request;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
