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

    // DAO-uri pentru persistenta + serviciul de audit
    private final ClientDAO clientDAO = new ClientDAO();
    private final SoferDAO soferDAO = new SoferDAO();
    private final AuditService audit = AuditService.getInstance();

    public void adaugaUtilizator(Utilizator u) {
        utilizatori.add(u);

        // persistam in baza de date in functie de tipul concret (mostenire + polimorfism)
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
        return null; // Nu am gasit niciunul liber
    }

    /** Acces la DAO pentru demonstratia CRUD din Main. */
    public ClientDAO getClientDAO() { return clientDAO; }
    public SoferDAO getSoferDAO() { return soferDAO; }
}