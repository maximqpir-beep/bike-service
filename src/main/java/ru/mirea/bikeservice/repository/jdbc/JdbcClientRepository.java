package ru.mirea.bikeservice.repository.jdbc;

import java.sql.*;
import ru.mirea.bikeservice.model.Client;
import ru.mirea.bikeservice.repository.ClientRepository;
import ru.mirea.bikeservice.util.ConnectionProvider;
public class JdbcClientRepository extends JdbcRepository<Client> implements ClientRepository {
    public JdbcClientRepository(ConnectionProvider c) { super(c, "clients", "full_name,phone,email"); }
    @Override protected Client map(ResultSet rs) throws SQLException { return new Client(rs.getLong("id"), rs.getString("full_name"), rs.getString("phone"), rs.getString("email")); }
    @Override protected void bind(PreparedStatement ps, Client c) throws SQLException { ps.setString(1,c.getFullName()); ps.setString(2,c.getPhone()); ps.setString(3,c.getEmail()); }
    @Override protected Long id(Client c) { return c.getId(); }
    @Override protected void setId(Client c, long id) { c.setId(id); }
}
