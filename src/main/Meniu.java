package main;

import models.*;
import services.OrderService;
import services.RestaurantService;
import services.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

// Interactive terminal menu that lets you trigger every feature required by the project.
public class Meniu {

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final Scanner scanner = new Scanner(System.in);

    public Meniu(UserService userService, RestaurantService restaurantService, OrderService orderService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    public void start() {
        // load existing data from the DB so it shows up and can be selected across runs
        userService.incarcaDinDB();
        restaurantService.incarcaDinDB();
        orderService.incarcaDinDB();

        boolean ruleaza = true;
        while (ruleaza) {
            System.out.println("\n=== PLATFORMA FOOD DELIVERY ===");
            System.out.println("1. Utilizatori");
            System.out.println("2. Restaurante");
            System.out.println("3. Comenzi");
            System.out.println("4. Audit");
            System.out.println("5. Demo automat (ruleaza scenariul complet)");
            System.out.println("0. Iesire");
            switch (citesteInt("Alege: ")) {
                case 1: meniuUtilizatori(); break;
                case 2: meniuRestaurante(); break;
                case 3: meniuComenzi(); break;
                case 4: afiseazaAudit(); break;
                case 5: demoAutomat(); break;
                case 0: ruleaza = false; System.out.println("La revedere!"); break;
                default: System.out.println("Optiune invalida.");
            }
        }
    }

    // ---------- 1. Utilizatori ----------
    private void meniuUtilizatori() {
        System.out.println("\n--- Utilizatori ---");
        System.out.println("1) Adauga client");
        System.out.println("2) Adauga sofer");
        System.out.println("3) Listeaza utilizatori din DB");
        System.out.println("4) Gaseste sofer disponibil");
        System.out.println("5) Actualizeaza telefon client");
        System.out.println("6) Sterge client");
        switch (citesteInt("Alege: ")) {
            case 1: adaugaClient(); break;
            case 2: adaugaSofer(); break;
            case 3: listeazaUtilizatoriDinDB(); break;
            case 4: gasesteSofer(); break;
            case 5: actualizeazaClient(); break;
            case 6: stergeClient(); break;
            default: System.out.println("Optiune invalida.");
        }
    }

    private void adaugaClient() {
        String nume = citesteString("Nume: ");
        String telefon = citesteString("Telefon: ");
        String oras = citesteString("Oras: ");
        String strada = citesteString("Strada: ");
        Client c = new Client(0, nume, telefon, new Adresa(oras, strada));
        userService.adaugaUtilizator(c); // persista in DB + audit, seteaza id-ul
        System.out.println("Client adaugat cu id " + c.getId());
    }

    private void adaugaSofer() {
        String nume = citesteString("Nume: ");
        String telefon = citesteString("Telefon: ");
        String numar = citesteString("Numar inmatriculare: ");
        Sofer s = new Sofer(0, nume, telefon, numar);
        userService.adaugaUtilizator(s);
        System.out.println("Sofer adaugat cu id " + s.getId());
    }

    private void listeazaUtilizatoriDinDB() {
        System.out.println("\nClienti in DB:");
        for (Client c : userService.getClientDAO().readAll()) {
            System.out.println("  #" + c.getId() + " " + c.getNume() + " - " + c.getTelefon()
                    + " - " + c.getAdresaLivrare().getOras());
        }
        System.out.println("Soferi in DB:");
        for (Sofer s : userService.getSoferDAO().readAll()) {
            System.out.println("  #" + s.getId() + " " + s.getNume() + " (" + s.getNumarInmatriculare()
                    + ") - disponibil: " + s.isDisponibil());
        }
    }

    private void gasesteSofer() {
        Sofer s = userService.gasesteSoferDisponibil();
        if (s != null) {
            System.out.println("Sofer disponibil: " + s.getNume() + " (" + s.getNumarInmatriculare() + ")");
        } else {
            System.out.println("Niciun sofer disponibil.");
        }
    }

    private void actualizeazaClient() {
        listeazaUtilizatoriDinDB();
        int id = citesteInt("Id client de actualizat: ");
        userService.getClientDAO().read(id).ifPresentOrElse(c -> {
            String telefonNou = citesteString("Telefon nou: ");
            userService.getClientDAO().update(new Client(c.getId(), c.getNume(), telefonNou, c.getAdresaLivrare()));
            System.out.println("Telefon actualizat pentru clientul #" + id);
        }, () -> System.out.println("Nu exista client cu id " + id));
    }

    private void stergeClient() {
        listeazaUtilizatoriDinDB();
        int id = citesteInt("Id client de sters: ");
        try {
            userService.getClientDAO().delete(id);
            System.out.println("Client #" + id + " sters.");
        } catch (RuntimeException e) {
            System.out.println("Nu pot sterge (are comenzi asociate?). " + e.getMessage());
        }
    }

    // ---------- 2. Restaurante ----------
    private void meniuRestaurante() {
        System.out.println("\n--- Restaurante ---");
        System.out.println("1) Adauga restaurant");
        System.out.println("2) Adauga produs in meniu");
        System.out.println("3) Afiseaza restaurante (sortate)");
        System.out.println("4) Listeaza restaurante din DB");
        System.out.println("5) Redenumeste restaurant");
        System.out.println("6) Sterge restaurant");
        switch (citesteInt("Alege: ")) {
            case 1: adaugaRestaurant(); break;
            case 2: adaugaProdus(); break;
            case 3: restaurantService.afiseazaRestaurante(); break;
            case 4: listeazaRestauranteDinDB(); break;
            case 5: actualizeazaRestaurant(); break;
            case 6: stergeRestaurant(); break;
            default: System.out.println("Optiune invalida.");
        }
    }

    private void adaugaRestaurant() {
        String nume = citesteString("Nume restaurant: ");
        Restaurant r = new Restaurant(nume);
        restaurantService.adaugaRestaurant(r); // persista + audit, intra in TreeSet sortat
        System.out.println("Restaurant adaugat cu id " + r.getId());
    }

    private void adaugaProdus() {
        Restaurant r = alegeRestaurant();
        if (r == null) return;
        String den = citesteString("Denumire produs: ");
        double pret = citesteDouble("Pret: ");
        if (pret < 0) { System.out.println("Pret invalid."); return; }
        restaurantService.adaugaProdusInMeniu(r, new Produs(den, pret));
        System.out.println("Produs adaugat in meniul restaurantului " + r.getNume());
    }

    private void listeazaRestauranteDinDB() {
        System.out.println("\nRestaurante in DB:");
        for (Restaurant r : restaurantService.getRestaurantDAO().readAll()) {
            System.out.println("  #" + r.getId() + " " + r.getNume());
        }
    }

    private void actualizeazaRestaurant() {
        Restaurant r = alegeRestaurant();
        if (r == null) return;
        String numeNou = citesteString("Nume nou: ");
        // updates the in-memory TreeSet AND the DB, so "Afiseaza restaurante" reflects the new name
        restaurantService.redenumesteRestaurant(r, numeNou);
        System.out.println("Restaurant #" + r.getId() + " redenumit in '" + numeNou + "'.");
    }

    private void stergeRestaurant() {
        listeazaRestauranteDinDB();
        int id = citesteInt("Id restaurant de sters: ");
        try {
            restaurantService.getRestaurantDAO().delete(id);
            System.out.println("Restaurant #" + id + " sters.");
        } catch (RuntimeException e) {
            System.out.println("Nu pot sterge (are comenzi asociate?). " + e.getMessage());
        }
    }

    // ---------- 3. Comenzi ----------
    private void meniuComenzi() {
        System.out.println("\n--- Comenzi ---");
        System.out.println("1) Plaseaza comanda");
        System.out.println("2) Proceseaza plata");
        System.out.println("3) Aloca sofer");
        System.out.println("4) Finalizeaza comanda");
        System.out.println("5) Istoric comenzi client");
        System.out.println("6) Listeaza comenzi din DB");
        switch (citesteInt("Alege: ")) {
            case 1: plaseazaComanda(); break;
            case 2: proceseazaPlata(); break;
            case 3: alocaSofer(); break;
            case 4: finalizeazaComanda(); break;
            case 5: istoricClient(); break;
            case 6: listeazaComenziDinDB(); break;
            default: System.out.println("Optiune invalida.");
        }
    }

    private void plaseazaComanda() {
        Client client = alegeClient();
        if (client == null) return;
        Restaurant restaurant = alegeRestaurant();
        if (restaurant == null) return;

        List<Produs> produse = new ArrayList<>();
        List<Produs> meniu = restaurant.getMeniu();
        if (!meniu.isEmpty()) {
            System.out.println("Produse din meniu:");
            for (int i = 0; i < meniu.size(); i++) {
                System.out.println("  " + (i + 1) + ") " + meniu.get(i).getDenumire() + " - " + meniu.get(i).getPret() + " RON");
            }
            String linie = citesteString("Alege produse (numere separate prin spatiu, gol = introduc manual): ");
            for (String tok : linie.split("\\s+")) {
                if (tok.isEmpty()) continue;
                try {
                    int idx = Integer.parseInt(tok) - 1;
                    if (idx >= 0 && idx < meniu.size()) produse.add(meniu.get(idx));
                } catch (NumberFormatException ignored) { }
            }
        }
        if (produse.isEmpty()) { // introducere manuala
            while (true) {
                String den = citesteString("Denumire produs (gol = stop): ");
                if (den.isEmpty()) break;
                double pret = citesteDouble("Pret: ");
                if (pret < 0) { System.out.println("Pret invalid."); continue; }
                produse.add(new Produs(den, pret));
            }
        }
        if (produse.isEmpty()) { System.out.println("Comanda fara produse, anulata."); return; }

        Comanda c = orderService.plaseazaComanda(client, restaurant, produse);
        System.out.println("Total comanda: " + c.calculeazaTotal() + " RON");
    }

    private void proceseazaPlata() {
        Comanda c = alegeComanda();
        if (c == null) return;
        String metoda = citesteString("Metoda de plata: ");
        orderService.proceseazaPlata(c, metoda);
    }

    private void alocaSofer() {
        Comanda c = alegeComanda();
        if (c == null) return;
        Sofer s = userService.gasesteSoferDisponibil();
        orderService.alocaSofer(c, s);
    }

    private void finalizeazaComanda() {
        Comanda c = alegeComanda();
        if (c == null) return;
        orderService.finalizeazaComanda(c);
    }

    private void istoricClient() {
        Client client = alegeClient();
        if (client == null) return;
        orderService.istoricComenziClient(client);
    }

    private void listeazaComenziDinDB() {
        System.out.println("\nComenzi in DB:");
        for (Comanda c : orderService.getComandaDAO().readAll()) {
            String numeClient = (c.getClient() != null) ? c.getClient().getNume() : "?";
            double total = (c.getPlata() != null) ? c.getPlata().getSuma() : c.calculeazaTotal();
            System.out.println("  #" + c.getId() + " - client: " + numeClient
                    + " - status: " + c.getStatus() + " - total: " + total + " RON");
        }
    }

    // ---------- 4. Audit ----------
    private void afiseazaAudit() {
        Path p = Path.of("audit.csv");
        if (!Files.exists(p)) { System.out.println("Nu exista inca fisierul audit.csv."); return; }
        System.out.println("\n--- Continut audit.csv ---");
        try {
            for (String linie : Files.readAllLines(p)) {
                System.out.println(linie);
            }
        } catch (IOException e) {
            System.out.println("Nu pot citi audit.csv: " + e.getMessage());
        }
    }

    // ---------- 5. Demo automat (scenariul scriptat original) ----------
    private void demoAutomat() {
        System.out.println("\n=== DEMO AUTOMAT ===");
        Client client1 = new Client(0, "Ion Popescu", "0722000000", new Adresa("Bucuresti", "Str. Victoriei"));
        Sofer sofer1 = new Sofer(0, "Marian", "0733000000", "B-100-ABC");
        userService.adaugaUtilizator(client1);
        userService.adaugaUtilizator(sofer1);

        Restaurant burgerShop = new Restaurant("Burger Shop");
        Restaurant asianWok = new Restaurant("Asian Wok");
        restaurantService.adaugaRestaurant(burgerShop);
        restaurantService.adaugaRestaurant(asianWok);

        Produs p1 = new Produs("Cheeseburger", 35.5);
        Produs p2 = new Produs("Cartofi Prajiti", 12.0);
        restaurantService.adaugaProdusInMeniu(burgerShop, p1);
        restaurantService.adaugaProdusInMeniu(burgerShop, p2);

        restaurantService.afiseazaRestaurante();

        Comanda comanda1 = orderService.plaseazaComanda(client1, burgerShop, Arrays.asList(p1, p2));
        orderService.proceseazaPlata(comanda1, "Card Bancar");
        Sofer soferDisponibil = userService.gasesteSoferDisponibil();
        orderService.alocaSofer(comanda1, soferDisponibil);
        orderService.finalizeazaComanda(comanda1);
        orderService.istoricComenziClient(client1);
        System.out.println("Comenzi in baza de date: " + orderService.getComandaDAO().readAll().size());
    }

    // ---------- helpers selectie (citesc din colectiile serviciilor) ----------
    private Client alegeClient() {
        List<Client> clienti = userService.getClienti();
        if (clienti.isEmpty()) { System.out.println("Nu exista clienti. Adauga unul intai."); return null; }
        for (int i = 0; i < clienti.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + clienti.get(i).getNume());
        }
        int idx = citesteInt("Alege client (numar): ") - 1;
        if (idx < 0 || idx >= clienti.size()) { System.out.println("Selectie invalida."); return null; }
        return clienti.get(idx);
    }

    private Restaurant alegeRestaurant() {
        List<Restaurant> restaurante = new ArrayList<>(restaurantService.getRestaurante());
        if (restaurante.isEmpty()) { System.out.println("Nu exista restaurante. Adauga unul intai."); return null; }
        for (int i = 0; i < restaurante.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + restaurante.get(i).getNume());
        }
        int idx = citesteInt("Alege restaurant (numar): ") - 1;
        if (idx < 0 || idx >= restaurante.size()) { System.out.println("Selectie invalida."); return null; }
        return restaurante.get(idx);
    }

    private Comanda alegeComanda() {
        List<Comanda> comenzi = orderService.getComenzi();
        if (comenzi.isEmpty()) { System.out.println("Nu exista comenzi. Plaseaza una intai."); return null; }
        for (int i = 0; i < comenzi.size(); i++) {
            System.out.println("  " + (i + 1) + ") Comanda #" + comenzi.get(i).getId() + " - " + comenzi.get(i).getStatus());
        }
        int idx = citesteInt("Alege comanda (numar): ") - 1;
        if (idx < 0 || idx >= comenzi.size()) { System.out.println("Selectie invalida."); return null; }
        return comenzi.get(idx);
    }

    // ---------- helpers input ----------
    private int citesteInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double citesteDouble(String prompt) {
        System.out.print(prompt);
        try {
            return Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String citesteString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
