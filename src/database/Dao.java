package database;

import java.util.List;
import java.util.Optional;

// Generic interface for the CRUD operations (Create, Read, Update, Delete).
// Every persisted entity has a DAO (Data Access Object) that implements this interface.
public interface Dao<T> {
    T create(T entity);             // INSERT; sets the generated id on the object and returns it
    Optional<T> read(int id);       // SELECT by id
    List<T> readAll();              // SELECT *
    void update(T entity);          // UPDATE by id
    void delete(int id);            // DELETE by id
}
