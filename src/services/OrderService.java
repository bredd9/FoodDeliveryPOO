package services;

import database.ComandaDAO;
import database.SoferDAO;
import models.Client;
import models.Comanda;
import models.Plata;
import models.Produs;
import models.Restaurant;
import models.Sofer;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private List<Comanda> comenzi = new ArrayList<>();

    private final ComandaDAO comandaDAO = new ComandaDAO();
    private final SoferDAO soferDAO = new SoferDAO();
    private final AuditService audit = AuditService.getInstance();

    public Comanda plaseazaComanda(Client client, Restaurant restaurant, List<Produs> produse) {
        Comanda comanda = new Comanda(client, restaurant, produse);
        comenzi.add(comanda);
        comandaDAO.create(comanda); // INSERT -> seteaza id-ul generat de DB
        System.out.println("\nComanda #" + comanda.getId() + " a fost plasata cu succes de " + client.getNume());
        audit.logActiune("plaseazaComanda");
        return comanda;
    }

    public void proceseazaPlata(Comanda comanda, String metoda) {
        double total = comanda.calculeazaTotal();
        Plata plata = new Plata(total, metoda);
        comanda.setPlata(plata);
        comandaDAO.update(comanda); // UPDATE: salvam plata pe comanda
        System.out.println("Plata in valoare de " + total + " RON a fost procesata (" + metoda + ").");
        audit.logActiune("proceseazaPlata");
    }

    // alocarea primeste soferul din exterior (de la UserService)
    public void alocaSofer(Comanda comanda, Sofer sofer) {
        if (sofer != null) {
            comanda.setSoferAsignat(sofer);
            sofer.setDisponibil(false);
            comanda.setStatus("In curs de livrare");
            comandaDAO.update(comanda); // UPDATE: sofer asignat + status
            soferDAO.update(sofer);     // UPDATE: soferul devine indisponibil
            System.out.println("Soferul " + sofer.getNume() + " a preluat comanda #" + comanda.getId());
        } else {
            System.out.println("Comanda #" + comanda.getId() + " este in asteptare. Niciun sofer disponibil!");
        }
        audit.logActiune("alocaSofer");
    }

    public void finalizeazaComanda(Comanda comanda) {
        comanda.setStatus("Livrata");
        comandaDAO.update(comanda); // UPDATE: status final

        // eliberam soferul asignat (daca exista) si persistam disponibilitatea
        Sofer sofer = comanda.getSoferAsignat();
        if (sofer != null) {
            sofer.setDisponibil(true);
            soferDAO.update(sofer);
        }
        System.out.println("Comanda #" + comanda.getId() + " a fost marcata ca Livrata.");
        audit.logActiune("finalizeazaComanda");
    }

    public void istoricComenziClient(Client client) {
        audit.logActiune("istoricComenziClient");
        System.out.println("\n--- Istoric Comenzi pentru " + client.getNume() + " ---");
        for (Comanda c : comenzi) {
            if (c.getClient().equals(client)) {
                System.out.println("Comanda #" + c.getId() + " - Status: " + c.getStatus() + " - Total: " + c.calculeazaTotal() + " RON");
            }
        }
    }

    /** Acces la DAO pentru demonstratia CRUD din Main. */
    public ComandaDAO getComandaDAO() { return comandaDAO; }
}
