package ru.mirea.bikeservice.service;

import java.math.*;
import java.util.*;
import ru.mirea.bikeservice.model.*;
public class StatisticsService {
    private final ClientService clients;
    private final ServiceRequestService requests;
    public StatisticsService(ClientService clients, ServiceRequestService requests) { this.clients = clients; this.requests = requests; }
    public Map<String, String> calculate() {
        List<ServiceRequest> all = requests.all();
        Map<String, String> result = new LinkedHashMap<>();
        result.put("Всего клиентов", String.valueOf(clients.all().size()));
        result.put("Всего заявок", String.valueOf(all.size()));
        for (RequestStatus s : RequestStatus.values()) result.put(s.name(), String.valueOf(all.stream().filter(r -> r.getStatus() == s).count()));
        List<BigDecimal> costs = all.stream().map(ServiceRequest::getEstimatedCost).filter(Objects::nonNull).toList();
        BigDecimal total = costs.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("Средняя указанная стоимость, руб.", costs.isEmpty() ? "нет данных" : total.divide(BigDecimal.valueOf(costs.size()), 2, RoundingMode.HALF_UP).toPlainString());
        result.put("Стоимость завершённых работ, руб.", all.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED)
            .map(ServiceRequest::getEstimatedCost).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2).toPlainString());
        return result;
    }
}
