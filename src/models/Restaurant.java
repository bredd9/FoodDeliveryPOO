package models;

import java.util.ArrayList;
import java.util.List;

public class Restaurant implements Comparable<Restaurant> {
    private int id;
    private String nume;
    private List<Produs> meniu;

    public Restaurant(String nume) {
        this.nume = nume;
        this.meniu = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNume() { return nume; }
    public void adaugaProdus(Produs p) { this.meniu.add(p); }
    public List<Produs> getMeniu() { return meniu; }

    // sort alphabetically by name, so it can be kept sorted inside a TreeSet
    @Override
    public int compareTo(Restaurant altRestaurant) {
        return this.nume.compareTo(altRestaurant.nume);
    }

    @Override
    public String toString() {
        return "Restaurant: " + nume;
    }
}
