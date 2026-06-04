package models;

public class Plata {
    private double suma;
    private String metodaPlata;

    public Plata(double suma, String metodaPlata) {
        this.suma = suma;
        this.metodaPlata = metodaPlata;
    }

    public double getSuma() { return suma; }
    public String getMetodaPlata() { return metodaPlata; }
}
