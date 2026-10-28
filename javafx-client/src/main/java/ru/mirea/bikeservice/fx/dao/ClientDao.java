package ru.mirea.bikeservice.fx.dao;

import ru.mirea.bikeservice.fx.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientDao {
    List<Client> findAll();
    Optional<Client> findById(long id);
}
