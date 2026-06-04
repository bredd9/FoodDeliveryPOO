# FoodDeliveryPOO — Food Delivery Platform

Individual project for the **Advanced Object-Oriented Programming (POO2)** course, theme **#8 – food delivery platform** (restaurants, orders, drivers, users).

A Java application that models a food delivery platform: clients place orders at restaurants, payments are processed, drivers are assigned, and an order history is kept. Data is persisted in a **PostgreSQL** database through **JDBC**, and every action in the system is recorded in a **CSV** audit file.

---

## Table of Contents
- [Project Requirements](#project-requirements)
- [Technologies](#technologies)
- [Architecture](#architecture)
- [Object Model (Stage I)](#object-model-stage-i)
- [System Actions (Stage I)](#system-actions-stage-i)
- [Persistence and Database (Stage II)](#persistence-and-database-stage-ii)
- [Audit Service (Stage II)](#audit-service-stage-ii)
- [Project Structure](#project-structure)
- [Setup and Running](#setup-and-running)
- [Example Output](#example-output)

---

## Project Requirements

### Stage I — OOP Modeling
- at least 8 object types and at least 10 actions/queries;
- classes with `private`/`protected` attributes and accessor methods;
- at least 2 different collections, of which at least one is sorted;
- use of **inheritance** for additional classes, used within collections;
- at least one **service class** exposing the system's operations;
- a `Main` class that calls the services.

### Stage II — Persistence + Audit
- persistence using a **relational database** and **JDBC**;
- **CRUD** services (create, read, update, delete) for at least **4** classes;
- **generic singleton services** for writing to and reading from the database;
- an **audit service** that writes to a **CSV** file on every action, with the structure `action_name, timestamp`.

---

## Technologies
- **Java** (no framework, just the JDK + standard JDBC)
- **PostgreSQL** as the relational database
- **JDBC driver**: `lib/postgresql-42.7.7.jar`
- IDE: IntelliJ IDEA (configuration in `FoodDeliveryPOO.iml`)

---

## Architecture

The application is organized in **three layers**, top to bottom:

```
Main
 │  calls the system services
 ▼
Services (UserService, RestaurantService, OrderService)
 │  every action: 1) business logic
 │                2) persistence via a DAO
 │                3) recording in the audit log (CSV)
 ▼
DAOs (ClientDAO, SoferDAO, RestaurantDAO, ComandaDAO)
 │  know the SQL for their table + how to rebuild the object (RowMapper)
 ▼
DatabaseService  (generic singleton)          AuditService (singleton)
 - a single shared JDBC connection              - writes audit.csv
 - generic query<T>(...) for READ
 - insert/update(...) for WRITE
```

**OOP patterns used:**
- **Inheritance + polymorphism**: `Utilizator` (abstract) → `Client`, `Sofer`; stored together in a `List<Utilizator>`.
- **Singleton**: `DatabaseService` and `AuditService` have a private constructor and a `getInstance()` method → a single shared instance (a single database connection).
- **Generics**: `DatabaseService.query(...)` and the `Dao<T>` interface work for any entity type, with no duplicated code.
- **Interface**: `Dao<T>` defines the CRUD contract common to all DAOs; `Restaurant` implements `Comparable<Restaurant>` for sorting.

---

## Object Model (Stage I)

The **8 object types** in the `models` package:

| Class | Role | Notes |
|---|---|---|
| `Utilizator` | abstract base class | `id`, `nume`, `telefon`; abstract method `afiseazaDetalii()` |
| `Client` | extends `Utilizator` | has a delivery `Adresa` |
| `Sofer` | extends `Utilizator` | has a license plate and a `disponibil` (available) flag |
| `Restaurant` | partner restaurant | implements `Comparable` (alphabetical sorting), has a menu of `Produs` |
| `Produs` | menu item | name + price |
| `Comanda` | placed order | links client, restaurant, products, driver, payment, status |
| `Plata` | payment for an order | amount + payment method |
| `Adresa` | delivery address | city + street |

> Note: class and field names are kept in Romanian because they are the actual identifiers in the source code (`Utilizator` = User, `Sofer` = Driver, `Comanda` = Order, `Plata` = Payment, `Adresa` = Address, `Produs` = Product, `nume` = name, `telefon` = phone, `disponibil` = available).

**Collections used:**
- `List<Utilizator>` and `List<Comanda>` (`ArrayList`) — unordered;
- `Set<Restaurant>` (`TreeSet`) — **sorted** automatically via `compareTo` (alphabetically by name).

---

## System Actions (Stage I)

The **10 actions** exposed by the service classes (each one is also audited):

| # | Action | Service |
|---|---|---|
| 1 | `adaugaUtilizator` (add user) | UserService |
| 2 | `gasesteSoferDisponibil` (find available driver) | UserService |
| 3 | `adaugaRestaurant` (add restaurant) | RestaurantService |
| 4 | `adaugaProdusInMeniu` (add product to menu) | RestaurantService |
| 5 | `afiseazaRestaurante` (list restaurants) | RestaurantService |
| 6 | `plaseazaComanda` (place order) | OrderService |
| 7 | `proceseazaPlata` (process payment) | OrderService |
| 8 | `alocaSofer` (assign driver) | OrderService |
| 9 | `finalizeazaComanda` (finalize order) | OrderService |
| 10 | `istoricComenziClient` (client order history) | OrderService |

---

## Persistence and Database (Stage II)

### Schema (`schema.sql`)

| Table | Columns |
|---|---|
| `restaurant` | `id` (SERIAL PK), `nume` |
| `client` | `id` (SERIAL PK), `nume`, `telefon`, `oras`, `strada` |
| `sofer` | `id` (SERIAL PK), `nume`, `telefon`, `numar_inmatriculare`, `disponibil` |
| `comanda` | `id` (SERIAL PK), `client_id` (FK), `restaurant_id` (FK), `sofer_id` (FK, nullable), `status`, `total`, `metoda_plata` |

`Adresa` is flattened into the `oras`/`strada` columns of `client`, and `Plata` into the `total`/`metoda_plata` columns of `comanda`. This gives exactly the 4 entities with CRUD that are required.

### Data Access Layer

- **`DatabaseService`** — a **generic singleton** service. It holds a single JDBC connection and provides:
  - `query(sql, RowMapper<T>, params...)` — generic read (returns `List<T>`);
  - `insert(sql, params...)` — INSERT, returns the generated id;
  - `update(sql, params...)` — UPDATE/DELETE, returns the number of affected rows.
- **`Dao<T>`** — a generic interface defining `create`, `read`, `readAll`, `update`, `delete`.
- **`ClientDAO`, `SoferDAO`, `RestaurantDAO`, `ComandaDAO`** — implement `Dao<T>` for each entity. Each has a `RowMapper` that rebuilds the object from a database row. `ComandaDAO` follows the foreign keys through the other DAOs.

### Order Lifecycle (CRUD example)
```
plaseazaComanda      -> INSERT order (status "Plasata" / Placed)
proceseazaPlata      -> UPDATE order (total + payment method)
alocaSofer           -> UPDATE order (sofer_id, status "In curs de livrare" / Out for delivery)
                        UPDATE driver (disponibil = false)
finalizeazaComanda   -> UPDATE order (status "Livrata" / Delivered)
                        UPDATE driver (disponibil = true)
```

---

## Audit Service (Stage II)

`AuditService` is a **singleton** that writes a line to the `audit.csv` file every time one of the actions is executed. The required structure:

```
nume_actiune,timestamp
adaugaUtilizator,2026-06-04T03:36:44.159816
plaseazaComanda,2026-06-04T03:36:44.169019
...
```

The file is created automatically with a header on the first run, and lines are appended, so the history is preserved across runs.

---

## Project Structure

```
FoodDeliveryPOO/
├── README.md
├── schema.sql                 # creates the tables in PostgreSQL
├── lib/
│   └── postgresql-42.7.7.jar  # JDBC driver
├── src/
│   ├── main/
│   │   └── Main.java          # demo: calls the services + CRUD demonstration
│   ├── models/                # the 8 model classes
│   │   ├── Utilizator.java
│   │   ├── Client.java
│   │   ├── Sofer.java
│   │   ├── Restaurant.java
│   │   ├── Produs.java
│   │   ├── Comanda.java
│   │   ├── Plata.java
│   │   └── Adresa.java
│   ├── services/              # service classes + audit
│   │   ├── UserService.java
│   │   ├── RestaurantService.java
│   │   ├── OrderService.java
│   │   └── AuditService.java
│   └── database/              # persistence layer (JDBC)
│       ├── DatabaseService.java   # generic singleton
│       ├── Dao.java               # generic CRUD interface
│       ├── ClientDAO.java
│       ├── SoferDAO.java
│       ├── RestaurantDAO.java
│       └── ComandaDAO.java
└── FoodDeliveryPOO.iml        # IntelliJ module configuration
```

---

## Setup and Running

### 1. Prepare the database
Requires PostgreSQL installed and running (locally). Create the database and the tables:
```bash
createdb fooddelivery
psql -d fooddelivery -f schema.sql
```
> The connection is configured in `src/database/DatabaseService.java` (URL, user, password). By default it uses `localhost:5432`, your local user, and an empty password ("trust" authentication). Change it there if your setup differs.

### 2. Compile
```bash
javac -cp lib/postgresql-42.7.7.jar -d out/production/FoodDeliveryPOO $(find src -name '*.java')
```

### 3. Run
```bash
java -cp "out/production/FoodDeliveryPOO:lib/postgresql-42.7.7.jar" main.Main
```

> In IntelliJ IDEA the project runs directly (the driver is already added to the classpath via `FoodDeliveryPOO.iml`).

### 4. Verify
```bash
# persisted data
psql -d fooddelivery -c 'SELECT * FROM comanda;'
# audit file
cat audit.csv
```
> To reset the data between runs, run `psql -d fooddelivery -f schema.sql` again.

---

## Example Output

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
