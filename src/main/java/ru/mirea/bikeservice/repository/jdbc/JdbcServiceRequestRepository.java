package ru.mirea.bikeservice.repository.jdbc;

import java.sql.*;
import ru.mirea.bikeservice.model.*;
import ru.mirea.bikeservice.repository.ServiceRequestRepository;
import ru.mirea.bikeservice.util.ConnectionProvider;
public class JdbcServiceRequestRepository extends JdbcRepository<ServiceRequest> implements ServiceRequestRepository {
    public JdbcServiceRequestRepository(ConnectionProvider c) { super(c, "service_requests", "client_id,bike_brand,bike_model,bike_type,problem_description,status,estimated_cost,created_at,completed_at"); }
    @Override protected ServiceRequest map(ResultSet rs) throws SQLException {
        Timestamp completed = rs.getTimestamp("completed_at");
        return new ServiceRequest(rs.getLong("id"), rs.getLong("client_id"), rs.getString("bike_brand"), rs.getString("bike_model"),
            BikeType.valueOf(rs.getString("bike_type")), rs.getString("problem_description"), RequestStatus.valueOf(rs.getString("status")),
            rs.getBigDecimal("estimated_cost"), rs.getTimestamp("created_at").toLocalDateTime(), completed == null ? null : completed.toLocalDateTime());
    }
    @Override protected void bind(PreparedStatement ps, ServiceRequest r) throws SQLException {
        ps.setLong(1,r.getClientId()); ps.setString(2,r.getBikeBrand()); ps.setString(3,r.getBikeModel());
        ps.setString(4,r.getBikeType().name()); ps.setString(5,r.getProblemDescription()); ps.setString(6,r.getStatus().name());
        ps.setBigDecimal(7,r.getEstimatedCost()); ps.setTimestamp(8,Timestamp.valueOf(r.getCreatedAt()));
        ps.setTimestamp(9,r.getCompletedAt() == null ? null : Timestamp.valueOf(r.getCompletedAt()));
    }
    @Override protected Long id(ServiceRequest r) { return r.getId(); }
    @Override protected void setId(ServiceRequest r, long id) { r.setId(id); }
}
