package services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

// Singleton audit service.
// Writes one line to a CSV file every time a system action runs.
// Required structure: nume_actiune, timestamp
public class AuditService {

    private static final String FISIER_CSV = "audit.csv";
    private static AuditService instance;

    private AuditService() {
        // if the file doesn't exist yet, write the header once
        File f = new File(FISIER_CSV);
        if (!f.exists()) {
            try (FileWriter writer = new FileWriter(f, true)) {
                writer.write("nume_actiune,timestamp\n");
            } catch (IOException e) {
                System.err.println("Could not initialize the audit file: " + e.getMessage());
            }
        }
    }

    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    // Appends a line to the CSV: the action name and when it ran.
    public void logActiune(String numeActiune) {
        try (FileWriter writer = new FileWriter(FISIER_CSV, true)) {
            writer.write(numeActiune + "," + LocalDateTime.now() + "\n");
        } catch (IOException e) {
            System.err.println("Failed to write to the audit file: " + e.getMessage());
        }
    }
}
