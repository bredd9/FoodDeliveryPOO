package database;

import java.util.List;
import java.util.Optional;

/**
 * Interfata generica pentru operatiile CRUD (Create, Read, Update, Delete).
 * Fiecare entitate persistata are un DAO (Data Access Object) care implementeaza aceasta interfata.
 */
public interface Dao<T> {
    T create(T entity);             // INSERT; seteaza id-ul generat pe obiect si il returneaza
    Optional<T> read(int id);       // SELECT dupa id
    List<T> readAll();              // SELECT *
    void update(T entity);          // UPDATE dupa id
    void delete(int id);            // DELETE dupa id
}
