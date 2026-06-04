package models;

public class Client extends Utilizator {
    private Adresa adresaLivrare;

    public Client(int id, String nume, String telefon, Adresa adresaLivrare) {
        super(id, nume, telefon);
        this.adresaLivrare = adresaLivrare;
    }

    public Adresa getAdresaLivrare() { return adresaLivrare; }

    @Override
    public void afiseazaDetalii() {
        System.out.println("Client: " + nume + " - Tel: " + telefon + " - Oraș: " + adresaLivrare.getOras());
    }
}
