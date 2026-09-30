package ru.mirea.bikeservice.repository.jdbc;

import java.sql.*;
import java.util.*;
import ru.mirea.bikeservice.repository.CrudRepository;
import ru.mirea.bikeservice.util.ConnectionProvider;
import ru.mirea.bikeservice.exception.DatabaseException;
public abstract class JdbcRepository<T> implements CrudRepository<T> {
    private final ConnectionProvider connections;
    private final String table;
    private final String columns;
    protected JdbcRepository(ConnectionProvider connections, String table, String columns) {
        this.connections = connections; this.table = table; this.columns = columns;
    }
    protected abstract T map(ResultSet rs) throws SQLException;
    protected abstract void bind(PreparedStatement ps, T entity) throws SQLException;
    protected abstract Long id(T entity);
    protected abstract void setId(T entity, long id);
    @Override public T save(T entity) {
        String placeholders = String.join(",", Collections.nCopies(columns.split(",").length, "?"));
        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, entity); ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Не получен ID новой записи");
                setId(entity, rs.getLong(1));
            }
            return entity;
        } catch (SQLException e) { throw new DatabaseException(e); }
    }
    @Override public List<T> findAll() { return select(null); }
    @Override public Optional<T> findById(long id) { return select(id).stream().findFirst(); }
    private List<T> select(Long id) {
        String sql = "SELECT * FROM " + table + (id == null ? " ORDER BY id" : " WHERE id = ?");
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (id != null) ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) result.add(map(rs));
                return result;
            }
        } catch (SQLException e) { throw new DatabaseException(e); }
    }
    @Override public boolean update(T entity) {
        String assignments = Arrays.stream(columns.split(",")).map(name -> name + " = ?").reduce((a,b) -> a + "," + b).orElseThrow();
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE " + table + " SET " + assignments + " WHERE id = ?")) {
            bind(ps, entity); ps.setLong(columns.split(",").length + 1, id(entity));
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new DatabaseException(e); }
    }
    @Override public boolean deleteById(long id) {
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM " + table + " WHERE id = ?")) {
            ps.setLong(1, id); return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new DatabaseException(e); }
    }
}
