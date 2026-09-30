package ru.mirea.bikeservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import ru.mirea.bikeservice.model.*;
import ru.mirea.bikeservice.repository.*;
import ru.mirea.bikeservice.exception.*;
public class ServiceRequestService {
    private final ServiceRequestRepository requests;
    private final ClientRepository clients;
    public ServiceRequestService(ServiceRequestRepository requests, ClientRepository clients) { this.requests = requests; this.clients = clients; }
    public List<ServiceRequest> all() { return requests.findAll(); }
    public ServiceRequest get(long id) { return requests.findById(id).orElseThrow(() -> new EntityNotFoundException("Заявка " + id + " не найдена.")); }
    public ServiceRequest create(ServiceRequest r) {
        validate(r); r.setId(null); r.setStatus(RequestStatus.CREATED); r.setCreatedAt(LocalDateTime.now()); r.setCompletedAt(null);
        return requests.save(r);
    }
    // Изменение данных не позволяет обойти отдельную проверку перехода статуса.
    public void update(ServiceRequest r) {
        ServiceRequest old = get(r.getId()); ensureEditable(old); validate(r);
        r.setStatus(old.getStatus()); r.setCreatedAt(old.getCreatedAt()); r.setCompletedAt(old.getCompletedAt()); persist(r);
    }
    public void delete(long id) { get(id); if (!requests.deleteById(id)) throw new EntityNotFoundException("Заявка не найдена."); }
    public void changeStatus(long id, RequestStatus next) {
        ServiceRequest r = get(id);
        if (next == null || !allowed(r.getStatus(), next)) throw new BusinessException("Переход " + r.getStatus() + " → " + next + " запрещён.");
        if (next == RequestStatus.COMPLETED && r.getEstimatedCost() == null) throw new BusinessException("Перед завершением укажите стоимость (можно 0).");
        r.setStatus(next); r.setCompletedAt(next == RequestStatus.COMPLETED ? LocalDateTime.now() : null); persist(r);
    }
    public static boolean allowed(RequestStatus from, RequestStatus to) {
        if (from == null || to == null) return false;
        return switch (from) {
            case CREATED -> to == RequestStatus.ACCEPTED || to == RequestStatus.CANCELLED;
            case ACCEPTED -> to == RequestStatus.IN_PROGRESS || to == RequestStatus.CANCELLED;
            case IN_PROGRESS -> to == RequestStatus.WAITING_FOR_PARTS || to == RequestStatus.COMPLETED || to == RequestStatus.CANCELLED;
            case WAITING_FOR_PARTS -> to == RequestStatus.IN_PROGRESS || to == RequestStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
    private void ensureEditable(ServiceRequest r) {
        if (r.getStatus() == RequestStatus.COMPLETED || r.getStatus() == RequestStatus.CANCELLED) throw new BusinessException("Завершённую или отменённую заявку нельзя изменять.");
    }
    private void persist(ServiceRequest r) { if (!requests.update(r)) throw new EntityNotFoundException("Заявка не найдена."); }
    private void validate(ServiceRequest r) {
        if (r.getClientId() == null || clients.findById(r.getClientId()).isEmpty()) throw new BusinessException("Указанный клиент не существует.");
        r.setBikeBrand(Validation.required(r.getBikeBrand(), "Марка", 100));
        r.setProblemDescription(Validation.required(r.getProblemDescription(), "Описание", 2000));
        if (r.getBikeModel() != null && r.getBikeModel().length() > 100) throw new BusinessException("Модель: максимум 100 символов.");
        if (r.getBikeType() == null) throw new BusinessException("Укажите тип велосипеда.");
        BigDecimal cost = r.getEstimatedCost();
        if (cost != null && (cost.signum() < 0 || cost.compareTo(new BigDecimal("99999999.99")) > 0 || cost.stripTrailingZeros().scale() > 2))
            throw new BusinessException("Стоимость: от 0 до 99999999.99, не более двух знаков после запятой.");
    }
    public List<ServiceRequest> searchBrand(String text) { String q = query(text); return all().stream().filter(r -> r.getBikeBrand().toLowerCase(Locale.ROOT).contains(q)).toList(); }
    public List<ServiceRequest> searchProblem(String text) { String q = query(text); return all().stream().filter(r -> r.getProblemDescription().toLowerCase(Locale.ROOT).contains(q)).toList(); }
    private String query(String text) { return Validation.required(text, "Поиск", 2000).toLowerCase(Locale.ROOT); }
    public List<ServiceRequest> filterStatus(RequestStatus s) { return all().stream().filter(r -> r.getStatus() == s).toList(); }
    public List<ServiceRequest> filterType(BikeType t) { return all().stream().filter(r -> r.getBikeType() == t).toList(); }
    public List<ServiceRequest> sortDate() { return all().stream().sorted(Comparator.comparing(ServiceRequest::getCreatedAt).thenComparing(ServiceRequest::getId)).toList(); }
    public List<ServiceRequest> sortCost() { return all().stream().sorted(Comparator.comparing(ServiceRequest::getEstimatedCost, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(ServiceRequest::getId)).toList(); }
}
