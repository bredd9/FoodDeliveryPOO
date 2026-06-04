package models;

public abstract class Utilizator {
    protected int id;
    protected String nume;
    protected String telefon;

    public Utilizator(int id, String nume, String telefon) {
        this.id = id;
        this.nume = nume;
        this.telefon = telefon;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }
    public String getTelefon() { return telefon; }

    public abstract void afiseazaDetalii();
}
