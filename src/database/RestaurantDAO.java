package database;

import models.Restaurant;

import java.util.List;
import java.util.Optional;

// CRUD operations for the Restaurant entity.
public class RestaurantDAO implements Dao<Restaurant> {

    private final DatabaseService db = DatabaseService.getInstance();

    // turns a table row into a Restaurant object
    private final DatabaseService.RowMapper<Restaurant> mapper = rs -> {
        Restaurant r = new Restaurant(rs.getString("nume"));
        r.setId(rs.getInt("id"));
        return r;
    };

    @Override
    public Restaurant create(Restaurant r) {
        int id = db.insert("INSERT INTO restaurant (nume) VALUES (?)", r.getNume());
        r.setId(id);
        return r;
    }

    @Override
    public Optional<Restaurant> read(int id) {
        List<Restaurant> rez = db.query("SELECT * FROM restaurant WHERE id = ?", mapper, id);
        return rez.stream().findFirst();
    }

    @Override
    public List<Restaurant> readAll() {
        return db.query("SELECT * FROM restaurant ORDER BY nume", mapper);
    }

    @Override
    public void update(Restaurant r) {
        db.update("UPDATE restaurant SET nume = ? WHERE id = ?", r.getNume(), r.getId());
    }

    @Override
    public void delete(int id) {
        db.update("DELETE FROM restaurant WHERE id = ?", id);
    }
}
