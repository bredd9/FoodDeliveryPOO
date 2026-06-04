package database;

import models.Adresa;
import models.Client;

import java.util.List;
import java.util.Optional;

/** DAO cu operatii CRUD pentru entitatea Client (adresa este aplatizata in coloanele oras/strada). */
public class ClientDAO implements Dao<Client> {

    private final DatabaseService db = DatabaseService.getInstance();

    // reconstruieste Client + Adresa din rand
    private final DatabaseService.RowMapper<Client> mapper = rs -> {
        Adresa adresa = new Adresa(rs.getString("oras"), rs.getString("strada"));
        Client c = new Client(rs.getInt("id"), rs.getString("nume"), rs.getString("telefon"), adresa);
        return c;
    };

    @Override
    public Client create(Client c) {
        int id = db.insert(
                "INSERT INTO client (nume, telefon, oras, strada) VALUES (?, ?, ?, ?)",
                c.getNume(), c.getTelefon(), c.getAdresaLivrare().getOras(), c.getAdresaLivrare().getStrada());
        c.setId(id);
        return c;
    }

    @Override
    public Optional<Client> read(int id) {
        List<Client> rez = db.query("SELECT * FROM client WHERE id = ?", mapper, id);
        return rez.stream().findFirst();
    }

    @Override
    public List<Client> readAll() {
        return db.query("SELECT * FROM client ORDER BY id", mapper);
    }

    @Override
    public void update(Client c) {
        db.update("UPDATE client SET nume = ?, telefon = ?, oras = ?, strada = ? WHERE id = ?",
                c.getNume(), c.getTelefon(), c.getAdresaLivrare().getOras(), c.getAdresaLivrare().getStrada(), c.getId());
    }

    @Override
    public void delete(int id) {
        db.update("DELETE FROM client WHERE id = ?", id);
    }
}
