package services;

import database.RestaurantDAO;
import models.Produs;
import models.Restaurant;
import java.util.Set;
import java.util.TreeSet;

public class RestaurantService {
    private Set<Restaurant> restaurante = new TreeSet<>();

    private final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private final AuditService audit = AuditService.getInstance();

    public void adaugaRestaurant(Restaurant r) {
        restaurante.add(r);        // sorted via compareTo
        restaurantDAO.create(r);   // persist to the DB (sets the generated id on the object)
        audit.logActiune("adaugaRestaurant");
    }

    public void adaugaProdusInMeniu(Restaurant r, Produs p) {
        r.adaugaProdus(p);
        audit.logActiune("adaugaProdusInMeniu");
    }

    // rename keeps the DB and the sorted TreeSet in sync: remove, rename, re-add (so it re-sorts), then UPDATE
    public void redenumesteRestaurant(Restaurant r, String numeNou) {
        restaurante.remove(r);     // remove using the old name (its current sort key)
        r.setNume(numeNou);
        restaurante.add(r);        // re-insert so the TreeSet re-sorts by the new name
        restaurantDAO.update(r);   // persist the change to the DB
        audit.logActiune("redenumesteRestaurant");
    }

    public void afiseazaRestaurante() {
        audit.logActiune("afiseazaRestaurante");
        System.out.println("\n--- Restaurante Partenere ---");
        for (Restaurant r : restaurante) {
            System.out.println(r.toString());

        }
    }

    // DAO access for the CRUD demo in Main
    public RestaurantDAO getRestaurantDAO() { return restaurantDAO; }
}
