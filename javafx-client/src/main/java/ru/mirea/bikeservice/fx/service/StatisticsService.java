package ru.mirea.bikeservice.fx.service;

import ru.mirea.bikeservice.fx.model.RequestStatus;
import ru.mirea.bikeservice.fx.model.ServiceRequest;
import ru.mirea.bikeservice.fx.model.Statistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class StatisticsService {
    public Statistics calculate(int clientCount, List<ServiceRequest> requests) {
        long active = requests.stream().filter(request -> switch (request.getStatus()) {
            case CREATED, ACCEPTED, IN_PROGRESS -> true;
            default -> false;
        }).count();
        long waiting = count(requests, RequestStatus.WAITING_FOR_PARTS);
        long completed = count(requests, RequestStatus.COMPLETED);
        long cancelled = count(requests, RequestStatus.CANCELLED);

        List<BigDecimal> costs = requests.stream()
                .map(ServiceRequest::getEstimatedCost)
                .filter(Objects::nonNull)
                .toList();
        BigDecimal total = costs.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = costs.isEmpty() ? BigDecimal.ZERO :
                total.divide(BigDecimal.valueOf(costs.size()), 2, RoundingMode.HALF_UP);
        BigDecimal revenue = requests.stream()
                .filter(request -> request.getStatus() == RequestStatus.COMPLETED)
                .map(ServiceRequest::getEstimatedCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Statistics(clientCount, requests.size(), active, waiting,
                completed, cancelled, average, revenue);
    }

    private long count(List<ServiceRequest> requests, RequestStatus status) {
        return requests.stream().filter(request -> request.getStatus() == status).count();
    }
}
