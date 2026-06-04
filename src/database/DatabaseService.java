package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviciu generic de tip singleton pentru scrierea si citirea din baza de date (PostgreSQL via JDBC).
 *
 * - singleton: o singura instanta, deci o singura conexiune partajata in toata aplicatia.
 * - generic: metoda {@link #query} poate citi orice tip de obiect printr-un {@link RowMapper}.
 *
 * Toate DAO-urile (ClientDAO, SoferDAO, etc.) folosesc acest serviciu pentru a vorbi cu baza de date.
 */
public class DatabaseService {

    // Datele de conectare. Postgres local foloseste "trust auth", deci parola este goala.
    private static final String URL = "jdbc:postgresql://localhost:5432/fooddelivery";
    private static final String USER = "stoicavlad";
    private static final String PASSWORD = ""; // schimba aici daca pui parola pe rol

    private static DatabaseService instance;
    private final Connection connection;

    // constructor privat => nimeni nu poate face "new DatabaseService()" din afara (pattern singleton)
    private DatabaseService() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Nu m-am putut conecta la baza de date: " + e.getMessage(), e);
        }
    }

    /** Punctul unic de acces la instanta singleton. */
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /**
     * Interfata generica de mapare: transforma un rand din ResultSet intr-un obiect de tip T.
     * Fiecare DAO isi defineste propriul RowMapper.
     */
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Citire generica (READ): ruleaza un SELECT si returneaza o lista de obiecte T.
     * Parametrii (?) din SQL sunt completati in ordine din {@code params}.
     */
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
            throw new RuntimeException("Eroare la interogare: " + sql + " -> " + e.getMessage(), e);
        }
        return rezultate;
    }

    /**
     * Scriere generica pentru INSERT: ruleaza comanda si returneaza id-ul generat (cheia primara SERIAL).
     */
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
            throw new RuntimeException("Eroare la insert: " + sql + " -> " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Scriere generica pentru UPDATE / DELETE: returneaza numarul de randuri afectate.
     */
    public int update(String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la update: " + sql + " -> " + e.getMessage(), e);
        }
    }

    private void bindParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
