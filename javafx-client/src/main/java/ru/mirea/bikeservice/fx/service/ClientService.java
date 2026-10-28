package ru.mirea.bikeservice.fx.service;

import ru.mirea.bikeservice.fx.dao.ClientDao;
import ru.mirea.bikeservice.fx.exception.EntityNotFoundException;
import ru.mirea.bikeservice.fx.model.Client;

import java.util.List;

public final class ClientService {
    private final ClientDao clients;

    public ClientService(ClientDao clients) { this.clients = clients; }

    public List<Client> findAll() { return clients.findAll(); }

    public Client get(long id) {
        return clients.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с ID " + id + " не найден."));
    }
}
