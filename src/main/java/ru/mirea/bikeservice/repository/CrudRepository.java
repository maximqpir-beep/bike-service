package ru.mirea.bikeservice.repository;

import java.util.*;
public interface CrudRepository<T> {
    T save(T entity);
    List<T> findAll();
    Optional<T> findById(long id);
    boolean update(T entity);
    boolean deleteById(long id);
}
