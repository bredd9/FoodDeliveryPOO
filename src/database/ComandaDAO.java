package database;

import models.Comanda;
import models.Plata;
import models.Sofer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO cu operatii CRUD pentru entitatea Comanda.
 * Comanda are chei straine catre client, restaurant si (optional) sofer.
 * Plata este aplatizata in coloanele total / metoda_plata.
 */
public class ComandaDAO implements Dao<Comanda> {

    private final DatabaseService db = DatabaseService.getInstance();
    private final ClientDAO clientDAO = new ClientDAO();
    private final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private final SoferDAO soferDAO = new SoferDAO();

    // reconstruieste Comanda urmarind cheile straine prin celelalte DAO-uri
    private final DatabaseService.RowMapper<Comanda> mapper = rs -> {
        int clientId = rs.getInt("client_id");
        int restaurantId = rs.getInt("restaurant_id");

        // produsele nu sunt persistate separat -> lista goala; totalul autoritativ vine din coloana "total"
        Comanda c = new Comanda(
                clientDAO.read(clientId).orElse(null),
                restaurantDAO.read(restaurantId).orElse(null),
                new ArrayList<>());
        c.setId(rs.getInt("id"));
        c.setStatus(rs.getString("status"));

        int soferId = rs.getInt("sofer_id");
        if (!rs.wasNull()) {
            soferDAO.read(soferId).ifPresent(c::setSoferAsignat);
        }

        String metoda = rs.getString("metoda_plata");
        if (metoda != null) {
            c.setPlata(new Plata(rs.getDouble("total"), metoda));
        }
        return c;
    };

    @Override
    public Comanda create(Comanda c) {
        Integer soferId = (c.getSoferAsignat() != null) ? c.getSoferAsignat().getId() : null;
        String metoda = (c.getPlata() != null) ? c.getPlata().getMetodaPlata() : null;
        int id = db.insert(
                "INSERT INTO comanda (client_id, restaurant_id, sofer_id, status, total, metoda_plata) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                c.getClient().getId(), c.getRestaurant().getId(), soferId,
                c.getStatus(), c.calculeazaTotal(), metoda);
        c.setId(id);
        return c;
    }

    @Override
    public Optional<Comanda> read(int id) {
        List<Comanda> rez = db.query("SELECT * FROM comanda WHERE id = ?", mapper, id);
        return rez.stream().findFirst();
    }

    @Override
    public List<Comanda> readAll() {
        return db.query("SELECT * FROM comanda ORDER BY id", mapper);
    }

    @Override
    public void update(Comanda c) {
        Sofer sofer = c.getSoferAsignat();
        Integer soferId = (sofer != null) ? sofer.getId() : null;
        double total = (c.getPlata() != null) ? c.getPlata().getSuma() : c.calculeazaTotal();
        String metoda = (c.getPlata() != null) ? c.getPlata().getMetodaPlata() : null;
        db.update("UPDATE comanda SET sofer_id = ?, status = ?, total = ?, metoda_plata = ? WHERE id = ?",
                soferId, c.getStatus(), total, metoda, c.getId());
    }

    @Override
    public void delete(int id) {
        db.update("DELETE FROM comanda WHERE id = ?", id);
    }
}
