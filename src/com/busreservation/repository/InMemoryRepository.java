package com.busreservation.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Thread-safe memory-based layout representing generic repository data
 * structures.
 */
public class InMemoryRepository<T, ID> implements Repository<T, ID> {
    private final Map<ID, T> database = new ConcurrentHashMap<>();
    private final Function<T, ID> keyExtractor;

    public InMemoryRepository(Function<T, ID> keyExtractor) {
        if (keyExtractor == null) {
            throw new IllegalArgumentException("Key extractor function cannot be null");
        }
        this.keyExtractor = keyExtractor;
    }

    @Override
    public void save(T entity) {
        if (entity == null)
            throw new IllegalArgumentException("Entity cannot be null");
        ID id = keyExtractor.apply(entity);
        if (id == null)
            throw new IllegalArgumentException("Cannot extract a null key from entity");
        database.put(id, entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        if (id == null)
            return Optional.empty();
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void deleteById(ID id) {
        if (id != null) {
            database.remove(id);
        }
    }
}
