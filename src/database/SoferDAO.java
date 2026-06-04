package database;

import models.Sofer;

import java.util.List;
import java.util.Optional;

// CRUD operations for the Sofer (driver) entity.
public class SoferDAO implements Dao<Sofer> {

    private final DatabaseService db = DatabaseService.getInstance();

    private final DatabaseService.RowMapper<Sofer> mapper = rs -> {
        Sofer s = new Sofer(rs.getInt("id"), rs.getString("nume"),
                rs.getString("telefon"), rs.getString("numar_inmatriculare"));
        s.setDisponibil(rs.getBoolean("disponibil"));
        return s;
    };

    @Override
    public Sofer create(Sofer s) {
        int id = db.insert(
                "INSERT INTO sofer (nume, telefon, numar_inmatriculare, disponibil) VALUES (?, ?, ?, ?)",
                s.getNume(), s.getTelefon(), s.getNumarInmatriculare(), s.isDisponibil());
        s.setId(id);
        return s;
    }

    @Override
    public Optional<Sofer> read(int id) {
        List<Sofer> rez = db.query("SELECT * FROM sofer WHERE id = ?", mapper, id);
        return rez.stream().findFirst();
    }

    @Override
    public List<Sofer> readAll() {
        return db.query("SELECT * FROM sofer ORDER BY id", mapper);
    }

    @Override
    public void update(Sofer s) {
        db.update("UPDATE sofer SET nume = ?, telefon = ?, numar_inmatriculare = ?, disponibil = ? WHERE id = ?",
                s.getNume(), s.getTelefon(), s.getNumarInmatriculare(), s.isDisponibil(), s.getId());
    }

    @Override
    public void delete(int id) {
        db.update("DELETE FROM sofer WHERE id = ?", id);
    }
}
