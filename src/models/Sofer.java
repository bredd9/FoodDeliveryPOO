package models;

public class Sofer extends Utilizator {
    private String numarInmatriculare;
    private boolean disponibil;

    public Sofer(int id, String nume, String telefon, String numarInmatriculare) {
        super(id, nume, telefon);
        this.numarInmatriculare = numarInmatriculare;
        this.disponibil = true; // implicit disponibil
    }

    public boolean isDisponibil() { return disponibil; }
    public void setDisponibil(boolean disponibil) { this.disponibil = disponibil; }
    public String getNumarInmatriculare() { return numarInmatriculare; }

    @Override
    public void afiseazaDetalii() {
        System.out.println("Sofer: " + nume + " (Masina: " + numarInmatriculare + ") - Disponibil: " + disponibil);
    }
}
