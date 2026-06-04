package main;

import models.*;
import services.OrderService;
import services.RestaurantService;
import services.UserService;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();
        RestaurantService restaurantService = new RestaurantService();
        OrderService orderService = new OrderService();

        // create users
        Adresa adresa1 = new Adresa("Bucuresti", "Str. Victoriei");
        Client client1 = new Client(1, "Ion Popescu", "0722000000", adresa1);
        Sofer sofer1 = new Sofer(2, "Marian", "0733000000", "B-100-ABC");

        userService.adaugaUtilizator(client1);
        userService.adaugaUtilizator(sofer1);

        // create restaurants and products
        Restaurant burgerShop = new Restaurant("Burger Shop");
        Restaurant asianWok = new Restaurant("Asian Wok");

        restaurantService.adaugaRestaurant(burgerShop);
        restaurantService.adaugaRestaurant(asianWok);

        Produs p1 = new Produs("Cheeseburger", 35.5);
        Produs p2 = new Produs("Cartofi Prajiti", 12.0);
        restaurantService.adaugaProdusInMeniu(burgerShop, p1);
        restaurantService.adaugaProdusInMeniu(burgerShop, p2);

        restaurantService.afiseazaRestaurante();

        // place an order
        Comanda comanda1 = orderService.plaseazaComanda(client1, burgerShop, Arrays.asList(p1, p2));

        // process the payment
        orderService.proceseazaPlata(comanda1, "Card Bancar");

        // find an available driver and assign it to the order
        Sofer soferDisponibil = userService.gasesteSoferDisponibil();
        orderService.alocaSofer(comanda1, soferDisponibil);

        // finalize the order (also frees up the driver, saved to the DB)
        orderService.finalizeazaComanda(comanda1);

        // order history
        orderService.istoricComenziClient(client1);

        // ====== CRUD demonstration (Stage II) ======
        System.out.println("\n=== Demonstratie CRUD prin DAO-uri (JDBC) ===");

        // READ: load the client back from the DB by id
        userService.getClientDAO().read(client1.getId()).ifPresent(c ->
                System.out.println("READ client #" + c.getId() + ": " + c.getNume() + " - " + c.getTelefon()));

        // UPDATE: change the client's phone number
        Client clientActualizat = userService.getClientDAO().read(client1.getId()).orElse(client1);
        userService.getClientDAO().update(
                new Client(clientActualizat.getId(), clientActualizat.getNume(), "0799999999", clientActualizat.getAdresaLivrare()));
        userService.getClientDAO().read(client1.getId()).ifPresent(c ->
                System.out.println("UPDATE client #" + c.getId() + " -> telefon nou: " + c.getTelefon()));

        // CREATE + DELETE: add a temporary restaurant, then remove it
        Restaurant temp = new Restaurant("Restaurant Temporar");
        restaurantService.getRestaurantDAO().create(temp);
        System.out.println("CREATE restaurant #" + temp.getId() + " (" + temp.getNume() + ")");
        restaurantService.getRestaurantDAO().delete(temp.getId());
        System.out.println("DELETE restaurant #" + temp.getId());

        // READ ALL: list all orders from the DB
        System.out.println("Comenzi in baza de date: " + orderService.getComandaDAO().readAll().size());
    }
}