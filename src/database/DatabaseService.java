package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Generic singleton service for reading from and writing to the database (PostgreSQL via JDBC).
// Singleton: one shared connection for the whole app. Generic: query() can read any type via a RowMapper.
// All DAOs go through this class to talk to the database.
public class DatabaseService {

    // Connection details. Local Postgres uses "trust" auth, so the password is empty.
    private static final String URL = "jdbc:postgresql://localhost:5432/fooddelivery";
    private static final String USER = "stoicavlad";
    private static final String PASSWORD = ""; // change here if you set a password on the role

    private static DatabaseService instance;
    private final Connection connection;

    // private constructor so nobody can do "new DatabaseService()" from outside (singleton)
    private DatabaseService() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the database: " + e.getMessage(), e);
        }
    }

    // single access point to the singleton instance
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    // Turns one row from a ResultSet into an object of type T. Each DAO defines its own mapper.
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    // Generic read: runs a SELECT and returns a list of objects. The ? placeholders are filled from params.
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        List<T> rezultate = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rezultate.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + sql + " -> " + e.getMessage(), e);
        }
        return rezultate;
    }

    // Write for INSERT: runs the statement and returns the generated id (the SERIAL primary key).
    public int insert(String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Insert failed: " + sql + " -> " + e.getMessage(), e);
        }
        return -1;
    }

    // Write for UPDATE / DELETE: returns the number of affected rows.
    public int update(String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update failed: " + sql + " -> " + e.getMessage(), e);
        }
    }

    private void bindParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
