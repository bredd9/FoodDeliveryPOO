package models;

import java.util.List;

public class Comanda {
    private static int counter = 1;
    private int id;
    private Client client;
    private Restaurant restaurant;
    private List<Produs> produse;
    private Sofer soferAsignat;
    private Plata plata;
    private String status;

    public Comanda(Client client, Restaurant restaurant, List<Produs> produse) {
        this.id = counter++;
        this.client = client;
        this.restaurant = restaurant;
        this.produse = produse;
        this.status = "Plasata";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Client getClient() { return client; }
    public Restaurant getRestaurant() { return restaurant; }
    public Sofer getSoferAsignat() { return soferAsignat; }
    public Plata getPlata() { return plata; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setSoferAsignat(Sofer sofer) { this.soferAsignat = sofer; }
    public void setPlata(Plata plata) { this.plata = plata; }

    public double calculeazaTotal() {
        double total = 0;
        for (Produs p : produse) {
            total += p.getPret();
        }
        return total;
    }
}
