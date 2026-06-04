-- Schema bazei de date pentru platforma Food Delivery (Etapa II)
-- Rulare: psql -d fooddelivery -f schema.sql

DROP TABLE IF EXISTS comanda CASCADE;
DROP TABLE IF EXISTS client CASCADE;
DROP TABLE IF EXISTS sofer CASCADE;
DROP TABLE IF EXISTS restaurant CASCADE;

CREATE TABLE restaurant (
    id   SERIAL PRIMARY KEY,
    nume VARCHAR(100) NOT NULL
);

CREATE TABLE client (
    id      SERIAL PRIMARY KEY,
    nume    VARCHAR(100) NOT NULL,
    telefon VARCHAR(20),
    oras    VARCHAR(100),
    strada  VARCHAR(150)
);

CREATE TABLE sofer (
    id                  SERIAL PRIMARY KEY,
    nume                VARCHAR(100) NOT NULL,
    telefon             VARCHAR(20),
    numar_inmatriculare VARCHAR(20),
    disponibil          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE comanda (
    id            SERIAL PRIMARY KEY,
    client_id     INTEGER NOT NULL REFERENCES client(id),
    restaurant_id INTEGER NOT NULL REFERENCES restaurant(id),
    sofer_id      INTEGER REFERENCES sofer(id),
    status        VARCHAR(40) NOT NULL,
    total         NUMERIC(10, 2) NOT NULL,
    metoda_plata  VARCHAR(40)
);