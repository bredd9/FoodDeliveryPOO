package models;

public class Adresa {
    private String oras;
    private String strada;

    public Adresa(String oras, String strada) {
        this.oras = oras;
        this.strada = strada;
    }

    public String getOras() { return oras; }
    public String getStrada() { return strada; }
}
