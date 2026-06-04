package services;

import database.ClientDAO;
import database.SoferDAO;
import models.Client;
import models.Sofer;
import models.Utilizator;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private List<Utilizator> utilizatori = new ArrayList<>();

    // DAOs for persistence + the audit service
    private final ClientDAO clientDAO = new ClientDAO();
    private final SoferDAO soferDAO = new SoferDAO();
    private final AuditService audit = AuditService.getInstance();

    // load existing users from the DB into memory (called once at startup)
    public void incarcaDinDB() {
        utilizatori.addAll(clientDAO.readAll());
        utilizatori.addAll(soferDAO.readAll());
    }

    // only the clients from the user list (used for selection in the menu)
    public List<Client> getClienti() {
        List<Client> clienti = new ArrayList<>();
        for (Utilizator u : utilizatori) {
            if (u instanceof Client) clienti.add((Client) u);
        }
        return clienti;
    }

    // delete a client from both the DB and the in-memory list
    public void stergeClient(int id) {
        clientDAO.delete(id);
        utilizatori.removeIf(u -> u instanceof Client && u.getId() == id);
        audit.logActiune("stergeClient");
    }

    // delete a driver from both the DB and the in-memory list
    public void stergeSofer(int id) {
        soferDAO.delete(id);
        utilizatori.removeIf(u -> u instanceof Sofer && u.getId() == id);
        audit.logActiune("stergeSofer");
    }

    public void adaugaUtilizator(Utilizator u) {
        utilizatori.add(u);

        // persist to the DB based on the concrete type (inheritance + polymorphism)
        if (u instanceof Client) {
            clientDAO.create((Client) u);
        } else if (u instanceof Sofer) {
            soferDAO.create((Sofer) u);
        }
        audit.logActiune("adaugaUtilizator");
    }

    public Sofer gasesteSoferDisponibil() {
        audit.logActiune("gasesteSoferDisponibil");
        for (Utilizator u : utilizatori) {
            if (u instanceof Sofer) {
                Sofer s = (Sofer) u;
                if (s.isDisponibil()) {
                    return s;
                }
            }
        }
        return null; // none available
    }

    // DAO access for the CRUD demo in Main
    public ClientDAO getClientDAO() { return clientDAO; }
    public SoferDAO getSoferDAO() { return soferDAO; }
}