# FoodDeliveryPOO — Platformă Food Delivery

Proiect individual pentru cursul **Programare Avansată pe Obiecte (POO2)**, tema **#8 – platformă food delivery** (localuri, comenzi, șoferi, useri).

Aplicație Java care modelează o platformă de livrare de mâncare: clienți care plasează comenzi la restaurante, plăți, alocare de șoferi și un istoric al comenzilor. Datele sunt persistate într-o bază de date **PostgreSQL** prin **JDBC**, iar fiecare acțiune din sistem este înregistrată într-un fișier de audit **CSV**.

---

## Cuprins
- [Cerințele proiectului](#cerințele-proiectului)
- [Tehnologii](#tehnologii)
- [Arhitectură](#arhitectură)
- [Modelul de obiecte (Etapa I)](#modelul-de-obiecte-etapa-i)
- [Acțiunile sistemului (Etapa I)](#acțiunile-sistemului-etapa-i)
- [Persistența și baza de date (Etapa II)](#persistența-și-baza-de-date-etapa-ii)
- [Serviciul de audit (Etapa II)](#serviciul-de-audit-etapa-ii)
- [Structura proiectului](#structura-proiectului)
- [Instalare și rulare](#instalare-și-rulare)
- [Exemplu de output](#exemplu-de-output)

---

## Cerințele proiectului

### Etapa I — Modelare OOP
- minim 8 tipuri de obiecte și minim 10 acțiuni/interogări;
- clase cu atribute `private`/`protected` și metode de acces;
- minim 2 colecții diferite, dintre care cel puțin una sortată;
- folosirea **moștenirii** pentru clase adiționale, utilizate în colecții;
- cel puțin o **clasă serviciu** care expune operațiile sistemului;
- o clasă `Main` care apelează serviciile.

### Etapa II — Persistență + Audit
- persistență cu o **bază de date relațională** și **JDBC**;
- servicii **CRUD** (create, read, update, delete) pentru cel puțin **4** clase;
- servicii **singleton generice** pentru scrierea și citirea din baza de date;
- un **serviciu de audit** care scrie într-un fișier **CSV** la fiecare acțiune, cu structura `nume_actiune, timestamp`.

---

## Tehnologii
- **Java** (fără framework, doar JDK + JDBC standard)
- **PostgreSQL** ca bază de date relațională
- **Driver JDBC**: `lib/postgresql-42.7.7.jar`
- IDE: IntelliJ IDEA (configurare în `FoodDeliveryPOO.iml`)

---

## Arhitectură

Aplicația este organizată pe **trei straturi**, de sus în jos:

```
Main
 │  apeleaza serviciile sistemului
 ▼
Servicii (UserService, RestaurantService, OrderService)
 │  fiecare actiune: 1) logica de business
 │                   2) persistare prin DAO
 │                   3) inregistrare in audit (CSV)
 ▼
DAO-uri (ClientDAO, SoferDAO, RestaurantDAO, ComandaDAO)
 │  stiu SQL-ul pentru tabelul lor + cum sa reconstruiasca obiectul (RowMapper)
 ▼
DatabaseService  (singleton generic)         AuditService (singleton)
 - o singura conexiune JDBC partajata          - scrie audit.csv
 - query<T>(...) generic pentru CITIRE
 - insert/update(...) pentru SCRIERE
```

**Pattern-uri OOP folosite:**
- **Moștenire + polimorfism**: `Utilizator` (abstract) → `Client`, `Sofer`; stocate împreună într-un `List<Utilizator>`.
- **Singleton**: `DatabaseService` și `AuditService` au constructor privat și `getInstance()` → o singură instanță partajată (o singură conexiune la DB).
- **Generic**: `DatabaseService.query(...)` și interfața `Dao<T>` funcționează pentru orice tip de entitate, fără cod duplicat.
- **Interfață**: `Dao<T>` definește contractul CRUD comun tuturor DAO-urilor; `Restaurant` implementează `Comparable<Restaurant>` pentru sortare.

---

## Modelul de obiecte (Etapa I)

Cele **8 tipuri de obiecte** din pachetul `models`:

| Clasă | Rol | Observații |
|---|---|---|
| `Utilizator` | clasă **abstractă** de bază | `id`, `nume`, `telefon`; metodă abstractă `afiseazaDetalii()` |
| `Client` | extinde `Utilizator` | are o `Adresa` de livrare |
| `Sofer` | extinde `Utilizator` | are număr de înmatriculare și flag `disponibil` |
| `Restaurant` | local partener | implementează `Comparable` (sortare alfabetică), are un meniu de `Produs` |
| `Produs` | produs din meniu | denumire + preț |
| `Comanda` | comandă plasată | leagă client, restaurant, produse, șofer, plată, status |
| `Plata` | plata unei comenzi | sumă + metodă de plată |
| `Adresa` | adresă de livrare | oraș + stradă |

**Colecții folosite:**
- `List<Utilizator>` și `List<Comanda>` (`ArrayList`) — neordonate;
- `Set<Restaurant>` (`TreeSet`) — **sortată** automat prin `compareTo` (alfabetic după nume).

---

## Acțiunile sistemului (Etapa I)

Cele **10 acțiuni** expuse de clasele serviciu (fiecare este și auditată):

| # | Acțiune | Serviciu |
|---|---|---|
| 1 | `adaugaUtilizator` | UserService |
| 2 | `gasesteSoferDisponibil` | UserService |
| 3 | `adaugaRestaurant` | RestaurantService |
| 4 | `adaugaProdusInMeniu` | RestaurantService |
| 5 | `afiseazaRestaurante` | RestaurantService |
| 6 | `plaseazaComanda` | OrderService |
| 7 | `proceseazaPlata` | OrderService |
| 8 | `alocaSofer` | OrderService |
| 9 | `finalizeazaComanda` | OrderService |
| 10 | `istoricComenziClient` | OrderService |

---

## Persistența și baza de date (Etapa II)

### Schema (`schema.sql`)

| Tabel | Coloane |
|---|---|
| `restaurant` | `id` (SERIAL PK), `nume` |
| `client` | `id` (SERIAL PK), `nume`, `telefon`, `oras`, `strada` |
| `sofer` | `id` (SERIAL PK), `nume`, `telefon`, `numar_inmatriculare`, `disponibil` |
| `comanda` | `id` (SERIAL PK), `client_id` (FK), `restaurant_id` (FK), `sofer_id` (FK, nullable), `status`, `total`, `metoda_plata` |

`Adresa` este aplatizată în coloanele `oras`/`strada` din `client`, iar `Plata` în coloanele `total`/`metoda_plata` din `comanda`. Astfel avem exact cele 4 entități cu CRUD cerute.

### Stratul de acces la date

- **`DatabaseService`** — serviciu **singleton generic**. Deține o singură conexiune JDBC și oferă:
  - `query(sql, RowMapper<T>, params...)` — citire generică (returnează `List<T>`);
  - `insert(sql, params...)` — INSERT, returnează id-ul generat;
  - `update(sql, params...)` — UPDATE/DELETE, returnează numărul de rânduri afectate.
- **`Dao<T>`** — interfață generică ce definește `create`, `read`, `readAll`, `update`, `delete`.
- **`ClientDAO`, `SoferDAO`, `RestaurantDAO`, `ComandaDAO`** — implementează `Dao<T>` pentru fiecare entitate. Fiecare are un `RowMapper` care reconstruiește obiectul dintr-un rând din baza de date. `ComandaDAO` urmărește cheile străine prin celelalte DAO-uri.

### Ciclul de viață al unei comenzi (exemplu CRUD)
```
plaseazaComanda      -> INSERT comanda (status "Plasata")
proceseazaPlata      -> UPDATE comanda (total + metoda de plata)
alocaSofer           -> UPDATE comanda (sofer_id, status "In curs de livrare")
                        UPDATE sofer   (disponibil = false)
finalizeazaComanda   -> UPDATE comanda (status "Livrata")
                        UPDATE sofer   (disponibil = true)
```

---

## Serviciul de audit (Etapa II)

`AuditService` este un **singleton** care scrie în fișierul `audit.csv` câte o linie de fiecare dată când se execută una dintre acțiuni. Structura cerută:

```
nume_actiune,timestamp
adaugaUtilizator,2026-06-04T03:36:44.159816
plaseazaComanda,2026-06-04T03:36:44.169019
...
```

Fișierul este creat automat cu antet la prima rulare, iar liniile sunt adăugate (append), deci istoricul se păstrează între rulări.

---

## Structura proiectului

```
FoodDeliveryPOO/
├── README.md
├── schema.sql                 # crearea tabelelor in PostgreSQL
├── lib/
│   └── postgresql-42.7.7.jar  # driver JDBC
├── src/
│   ├── main/
│   │   └── Main.java          # demo: apeleaza serviciile + demonstratie CRUD
│   ├── models/                # cele 8 clase de model
│   │   ├── Utilizator.java
│   │   ├── Client.java
│   │   ├── Sofer.java
│   │   ├── Restaurant.java
│   │   ├── Produs.java
│   │   ├── Comanda.java
│   │   ├── Plata.java
│   │   └── Adresa.java
│   ├── services/              # clasele serviciu + audit
│   │   ├── UserService.java
│   │   ├── RestaurantService.java
│   │   ├── OrderService.java
│   │   └── AuditService.java
│   └── database/              # stratul de persistenta (JDBC)
│       ├── DatabaseService.java   # singleton generic
│       ├── Dao.java               # interfata CRUD generica
│       ├── ClientDAO.java
│       ├── SoferDAO.java
│       ├── RestaurantDAO.java
│       └── ComandaDAO.java
└── FoodDeliveryPOO.iml        # configurare modul IntelliJ
```

---

## Instalare și rulare

### 1. Pregătirea bazei de date
Necesită PostgreSQL instalat și pornit (local). Creează baza de date și tabelele:
```bash
createdb fooddelivery
psql -d fooddelivery -f schema.sql
```
> Conexiunea este configurată în `src/database/DatabaseService.java` (URL, user, parolă). Implicit folosește `localhost:5432`, userul tău local și parolă goală (autentificare „trust"). Modifică acolo dacă ai altă configurație.

### 2. Compilare
```bash
javac -cp lib/postgresql-42.7.7.jar -d out/production/FoodDeliveryPOO $(find src -name '*.java')
```

### 3. Rulare
```bash
java -cp "out/production/FoodDeliveryPOO:lib/postgresql-42.7.7.jar" main.Main
```

> În IntelliJ IDEA proiectul rulează direct (driverul este deja adăugat în classpath prin `FoodDeliveryPOO.iml`).

### 4. Verificare
```bash
# datele persistate
psql -d fooddelivery -c 'SELECT * FROM comanda;'
# fisierul de audit
cat audit.csv
```
> Pentru a reseta datele între rulări, rulează din nou `psql -d fooddelivery -f schema.sql`.

---

## Exemplu de output

```
--- Restaurante Partenere ---
Restaurant: Asian Wok
Restaurant: Burger Shop

Comanda #1 a fost plasata cu succes de Ion Popescu
Plata in valoare de 47.5 RON a fost procesata (Card Bancar).
Soferul Marian a preluat comanda #1
Comanda #1 a fost marcata ca Livrata.

--- Istoric Comenzi pentru Ion Popescu ---
Comanda #1 - Status: Livrata - Total: 47.5 RON

=== Demonstratie CRUD prin DAO-uri (JDBC) ===
READ client #1: Ion Popescu - 0722000000
UPDATE client #1 -> telefon nou: 0799999999
CREATE restaurant #3 (Restaurant Temporar)
DELETE restaurant #3
Comenzi in baza de date: 1
```
