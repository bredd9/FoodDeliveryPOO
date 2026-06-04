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

        // creeare utilizatori
        Adresa adresa1 = new Adresa("Bucuresti", "Str. Victoriei");
        Client client1 = new Client(1, "Ion Popescu", "0722000000", adresa1);
        Sofer sofer1 = new Sofer(2, "Marian", "0733000000", "B-100-ABC");

        userService.adaugaUtilizator(client1);
        userService.adaugaUtilizator(sofer1);

        // creeare restaurante si produse
        Restaurant burgerShop = new Restaurant("Burger Shop");
        Restaurant asianWok = new Restaurant("Asian Wok");

        restaurantService.adaugaRestaurant(burgerShop);
        restaurantService.adaugaRestaurant(asianWok);

        Produs p1 = new Produs("Cheeseburger", 35.5);
        Produs p2 = new Produs("Cartofi Prajiti", 12.0);
        restaurantService.adaugaProdusInMeniu(burgerShop, p1);
        restaurantService.adaugaProdusInMeniu(burgerShop, p2);

        restaurantService.afiseazaRestaurante();

        // plasare comanda
        Comanda comanda1 = orderService.plaseazaComanda(client1, burgerShop, Arrays.asList(p1, p2));

        // procesare plata
        orderService.proceseazaPlata(comanda1, "Card Bancar");

        // cautam un sofer disponibil prin UserService si il alocam prin OrderService
        Sofer soferDisponibil = userService.gasesteSoferDisponibil();
        orderService.alocaSofer(comanda1, soferDisponibil);

        // finalizare comanda (elibereaza si soferul, persistat in DB)
        orderService.finalizeazaComanda(comanda1);

        // istoric
        orderService.istoricComenziClient(client1);

        // ====== DEMONSTRATIE CRUD (Etapa II) ======
        System.out.println("\n=== Demonstratie CRUD prin DAO-uri (JDBC) ===");

        // READ: citim clientul inapoi din baza de date dupa id
        userService.getClientDAO().read(client1.getId()).ifPresent(c ->
                System.out.println("READ client #" + c.getId() + ": " + c.getNume() + " - " + c.getTelefon()));

        // UPDATE: schimbam numarul de telefon al clientului
        client1.setNume(client1.getNume()); // numele ramane
        Client clientActualizat = userService.getClientDAO().read(client1.getId()).orElse(client1);
        userService.getClientDAO().update(
                new Client(clientActualizat.getId(), clientActualizat.getNume(), "0799999999", clientActualizat.getAdresaLivrare()));
        userService.getClientDAO().read(client1.getId()).ifPresent(c ->
                System.out.println("UPDATE client #" + c.getId() + " -> telefon nou: " + c.getTelefon()));

        // CREATE + DELETE: adaugam un restaurant temporar, apoi il stergem
        Restaurant temp = new Restaurant("Restaurant Temporar");
        restaurantService.getRestaurantDAO().create(temp);
        System.out.println("CREATE restaurant #" + temp.getId() + " (" + temp.getNume() + ")");
        restaurantService.getRestaurantDAO().delete(temp.getId());
        System.out.println("DELETE restaurant #" + temp.getId());

        // READ ALL: listam toate comenzile din baza de date
        System.out.println("Comenzi in baza de date: " + orderService.getComandaDAO().readAll().size());
    }
}