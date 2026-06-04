package services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Serviciu de audit de tip singleton.
 * Scrie cate o linie intr-un fisier CSV de fiecare data cand se executa o actiune din sistem.
 * Structura ceruta: nume_actiune, timestamp
 */
public class AuditService {

    private static final String FISIER_CSV = "audit.csv";
    private static AuditService instance;

    private AuditService() {
        // daca fisierul nu exista, scriem antetul (header) o singura data
        File f = new File(FISIER_CSV);
        if (!f.exists()) {
            try (FileWriter writer = new FileWriter(f, true)) {
                writer.write("nume_actiune,timestamp\n");
            } catch (IOException e) {
                System.err.println("Nu am putut initializa fisierul de audit: " + e.getMessage());
            }
        }
    }

    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    /** Adauga (append) o linie in CSV: numele actiunii si momentul executiei. */
    public void logActiune(String numeActiune) {
        try (FileWriter writer = new FileWriter(FISIER_CSV, true)) {
            writer.write(numeActiune + "," + LocalDateTime.now() + "\n");
        } catch (IOException e) {
            System.err.println("Eroare la scrierea in audit: " + e.getMessage());
        }
    }
}
