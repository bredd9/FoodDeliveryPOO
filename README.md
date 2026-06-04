# FoodDeliveryPOO — Food Delivery Platform

Individual project for **Advanced OOP (POO2)**, theme #8 — food delivery (restaurants, orders, drivers, users). A Java app with an **interactive terminal menu**, persistence in **PostgreSQL via JDBC**, and a **CSV audit log** of every action.

## Requirements covered

**Stage I (OOP):** 8 model classes · 10 service actions · inheritance (`Utilizator → Client, Sofer`) · 2 collections incl. a sorted `TreeSet` · service classes + `Main`.

**Stage II (persistence + audit):** JDBC persistence · full **CRUD on 4 entities** (Client, Sofer, Restaurant, Comanda) · a **generic singleton** DB service · a **CSV audit service** (`nume_actiune,timestamp`).

## Architecture

```
Main → Meniu (terminal UI)
        → Services (UserService, RestaurantService, OrderService)
             → DAOs (ClientDAO, SoferDAO, RestaurantDAO, ComandaDAO)   → DatabaseService → PostgreSQL
             → AuditService → audit.csv
```

- **Singleton:** `DatabaseService` (one shared JDBC connection) and `AuditService`.
- **Generic:** `DatabaseService.query(...)` + the `Dao<T>` CRUD interface — no duplicated SQL boilerplate.
- Services hold the in-memory collections (incl. the sorted `TreeSet` of restaurants) and are the single source of truth, loaded from the DB at startup so data persists across runs.

## The menu

`Main` launches an interactive menu (text in Romanian) that triggers every feature:

- **Utilizatori:** add client / add driver · list from DB · find available driver · update client phone · delete client / driver
- **Restaurante:** add restaurant · add product to menu · show sorted (TreeSet) · list from DB · rename · delete
- **Comenzi:** place · pay · assign driver · finalize · client history · list from DB
- **Audit:** print `audit.csv`
- **Demo automat:** runs the full scripted scenario in one shot

> Class/field names are in Romanian as they are the actual code identifiers (`Utilizator`=User, `Sofer`=Driver, `Comanda`=Order, `Plata`=Payment, etc.).

## Run

Requires PostgreSQL running locally. Connection is set in `src/database/DatabaseService.java` (default: `localhost:5432`, local user, trust auth).

```bash
createdb fooddelivery
psql -d fooddelivery -f schema.sql                 # create tables (also resets data)

javac -cp lib/postgresql-42.7.7.jar -d out/production/FoodDeliveryPOO $(find src -name '*.java')
java -cp "out/production/FoodDeliveryPOO:lib/postgresql-42.7.7.jar" main.Main
```

In IntelliJ just run `Main` (the JDBC driver is on the classpath via `FoodDeliveryPOO.iml`).

## Structure

```
src/
  main/      Main.java, Meniu.java
  models/    Utilizator, Client, Sofer, Restaurant, Produs, Comanda, Plata, Adresa
  services/  UserService, RestaurantService, OrderService, AuditService
  database/  DatabaseService, Dao, ClientDAO, SoferDAO, RestaurantDAO, ComandaDAO
schema.sql · lib/postgresql-42.7.7.jar
```
