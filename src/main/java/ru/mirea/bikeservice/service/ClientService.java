package ru.mirea.bikeservice.service;

import java.util.*;
import ru.mirea.bikeservice.model.Client;
import ru.mirea.bikeservice.repository.*;
import ru.mirea.bikeservice.exception.*;
public class ClientService {
    private final ClientRepository clients;
    private final ServiceRequestRepository requests;
    public ClientService(ClientRepository clients, ServiceRequestRepository requests) { this.clients = clients; this.requests = requests; }
    public List<Client> all() { return clients.findAll(); }
    public Client get(long id) { return clients.findById(id).orElseThrow(() -> new EntityNotFoundException("Клиент " + id + " не найден.")); }
    public Client create(Client c) { c.setId(null); validate(c); return clients.save(c); }
    public void update(Client c) { get(c.getId()); validate(c); if (!clients.update(c)) throw new EntityNotFoundException("Клиент не найден."); }
    public void delete(long id) {
        get(id);
        if (requests.findAll().stream().anyMatch(r -> r.getClientId().equals(id))) throw new BusinessException("Нельзя удалить клиента, у которого есть заявки.");
        if (!clients.deleteById(id)) throw new EntityNotFoundException("Клиент не найден.");
    }
    private void validate(Client c) {
        c.setFullName(Validation.required(c.getFullName(), "ФИО", 150));
        String phone = Validation.required(c.getPhone(), "Телефон", 30).replaceAll("[ ()-]", "");
        if (!phone.matches("\\+?[0-9]{10,15}")) throw new BusinessException("Телефон: 10–15 цифр и необязательный + в начале.");
        c.setPhone(phone);
        if (c.getEmail() != null && !c.getEmail().isBlank()) {
            String email = Validation.required(c.getEmail(), "Email", 150).toLowerCase(Locale.ROOT);
            if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new BusinessException("Некорректный email.");
            c.setEmail(email);
        } else c.setEmail(null);
        for (Client other : clients.findAll()) {
            if (Objects.equals(other.getId(), c.getId())) continue;
            if (other.getPhone().equals(c.getPhone())) throw new BusinessException("Телефон уже зарегистрирован.");
            if (c.getEmail() != null && c.getEmail().equalsIgnoreCase(other.getEmail())) throw new BusinessException("Email уже зарегистрирован.");
        }
    }
}
