package com.busreservation.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface abstracting data storage CRUD behaviors.
 */
public interface Repository<T, ID> {
    void save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    void deleteById(ID id);
}
