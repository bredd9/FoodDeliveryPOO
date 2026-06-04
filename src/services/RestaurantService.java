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
