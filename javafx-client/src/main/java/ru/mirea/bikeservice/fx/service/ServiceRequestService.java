package ru.mirea.bikeservice.fx.service;

import ru.mirea.bikeservice.fx.dao.ClientDao;
import ru.mirea.bikeservice.fx.dao.ServiceRequestDao;
import ru.mirea.bikeservice.fx.exception.BusinessException;
import ru.mirea.bikeservice.fx.exception.EntityNotFoundException;
import ru.mirea.bikeservice.fx.model.RequestStatus;
import ru.mirea.bikeservice.fx.model.ServiceRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ServiceRequestService {
    private static final BigDecimal MAX_COST = new BigDecimal("99999999.99");
    private final ServiceRequestDao requests;
    private final ClientDao clients;

    public ServiceRequestService(ServiceRequestDao requests, ClientDao clients) {
        this.requests = requests;
        this.clients = clients;
    }

    public List<ServiceRequest> findAll() { return requests.findAll(); }

    public ServiceRequest get(long id) {
        return requests.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заявка с ID " + id + " не найдена."));
    }

    public ServiceRequest create(ServiceRequest request) {
        validateFields(request);
        request.setId(null);
        request.setStatus(RequestStatus.CREATED);
        request.setCreatedAt(LocalDateTime.now());
        request.setCompletedAt(null);
        return requests.insert(request);
    }

    public void update(ServiceRequest edited) {
        if (edited.getId() == null) throw new BusinessException("У заявки отсутствует ID.");
        ServiceRequest current = get(edited.getId());
        validateFields(edited);

        if (isTerminal(current.getStatus())) {
            throw new BusinessException("Завершённую или отменённую заявку нельзя редактировать.");
        }
        if (edited.getStatus() == null) edited.setStatus(current.getStatus());
        if (edited.getStatus() != current.getStatus() && !isAllowedTransition(current.getStatus(), edited.getStatus())) {
            throw new BusinessException("Переход " + current.getStatus() + " → " + edited.getStatus() + " запрещён.");
        }
        if (edited.getStatus() == RequestStatus.COMPLETED && edited.getEstimatedCost() == null) {
            throw new BusinessException("Перед завершением укажите стоимость обслуживания.");
        }

        edited.setCreatedAt(current.getCreatedAt());
        edited.setCompletedAt(edited.getStatus() == RequestStatus.COMPLETED ? LocalDateTime.now() : null);
        if (!requests.update(edited)) throw new EntityNotFoundException("Заявка была удалена другим пользователем.");
    }

    public void delete(long id) {
        get(id);
        if (!requests.deleteById(id)) throw new EntityNotFoundException("Заявка уже удалена.");
    }

    public List<RequestStatus> allowedStatuses(RequestStatus current) {
        return switch (current) {
            case CREATED -> List.of(RequestStatus.CREATED, RequestStatus.ACCEPTED, RequestStatus.CANCELLED);
            case ACCEPTED -> List.of(RequestStatus.ACCEPTED, RequestStatus.IN_PROGRESS, RequestStatus.CANCELLED);
            case IN_PROGRESS -> List.of(RequestStatus.IN_PROGRESS, RequestStatus.WAITING_FOR_PARTS,
                    RequestStatus.COMPLETED, RequestStatus.CANCELLED);
            case WAITING_FOR_PARTS -> List.of(RequestStatus.WAITING_FOR_PARTS,
                    RequestStatus.IN_PROGRESS, RequestStatus.CANCELLED);
            case COMPLETED -> List.of(RequestStatus.COMPLETED);
            case CANCELLED -> List.of(RequestStatus.CANCELLED);
        };
    }

    public static boolean isAllowedTransition(RequestStatus from, RequestStatus to) {
        if (from == null || to == null) return false;
        return switch (from) {
            case CREATED -> to == RequestStatus.ACCEPTED || to == RequestStatus.CANCELLED;
            case ACCEPTED -> to == RequestStatus.IN_PROGRESS || to == RequestStatus.CANCELLED;
            case IN_PROGRESS -> to == RequestStatus.WAITING_FOR_PARTS ||
                    to == RequestStatus.COMPLETED || to == RequestStatus.CANCELLED;
            case WAITING_FOR_PARTS -> to == RequestStatus.IN_PROGRESS || to == RequestStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private void validateFields(ServiceRequest request) {
        if (request.getClientId() == null || clients.findById(request.getClientId()).isEmpty()) {
            throw new BusinessException("Выберите существующего клиента.");
        }
        request.setBikeBrand(required(request.getBikeBrand(), "Марка", 100));
        request.setProblemDescription(required(request.getProblemDescription(), "Описание неисправности", 2000));
        if (request.getBikeModel() != null) {
            request.setBikeModel(request.getBikeModel().trim());
            if (request.getBikeModel().length() > 100) throw new BusinessException("Модель: максимум 100 символов.");
        }
        if (request.getBikeType() == null) throw new BusinessException("Выберите тип велосипеда.");
        BigDecimal cost = request.getEstimatedCost();
        if (cost != null && (cost.signum() < 0 || cost.compareTo(MAX_COST) > 0 || cost.stripTrailingZeros().scale() > 2)) {
            throw new BusinessException("Стоимость: от 0 до 99999999.99, максимум два знака после запятой.");
        }
    }

    private String required(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) throw new BusinessException(field + ": обязательное поле.");
        String normalized = value.trim();
        if (normalized.length() > maxLength) throw new BusinessException(field + ": максимум " + maxLength + " символов.");
        return normalized;
    }

    private boolean isTerminal(RequestStatus status) {
        return status == RequestStatus.COMPLETED || status == RequestStatus.CANCELLED;
    }
}
