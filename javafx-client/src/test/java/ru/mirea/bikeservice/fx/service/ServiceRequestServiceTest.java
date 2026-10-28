package ru.mirea.bikeservice.fx.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mirea.bikeservice.fx.dao.ClientDao;
import ru.mirea.bikeservice.fx.dao.ServiceRequestDao;
import ru.mirea.bikeservice.fx.exception.BusinessException;
import ru.mirea.bikeservice.fx.model.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRequestServiceTest {
    private InMemoryRequestDao requestDao;
    private ServiceRequestService service;

    @BeforeEach
    void setUp() {
        requestDao = new InMemoryRequestDao();
        Client client = new Client(1L, "Иван Петров", "+79000000001", "ivan@example.com");
        ClientDao clientDao = new ClientDao() {
            @Override public List<Client> findAll() { return List.of(client); }
            @Override public Optional<Client> findById(long id) {
                return id == 1 ? Optional.of(client) : Optional.empty();
            }
        };
        service = new ServiceRequestService(requestDao, clientDao);
    }

    @Test
    void createForcesCreatedStatusAndGeneratedId() {
        ServiceRequest request = validRequest();
        request.setStatus(RequestStatus.COMPLETED);

        ServiceRequest created = service.create(request);

        assertEquals(1L, created.getId());
        assertEquals(RequestStatus.CREATED, created.getStatus());
        assertNotNull(created.getCreatedAt());
        assertNull(created.getCompletedAt());
    }

    @Test
    void rejectsMissingClientAndInvalidCost() {
        ServiceRequest request = validRequest();
        request.setClientId(999L);
        assertThrows(BusinessException.class, () -> service.create(request));

        request.setClientId(1L);
        request.setEstimatedCost(new BigDecimal("-1"));
        assertThrows(BusinessException.class, () -> service.create(request));
    }

    @Test
    void enforcesStatusTransitionsAndCompletionCost() {
        ServiceRequest created = service.create(validRequest());
        ServiceRequest illegal = created.copy();
        illegal.setStatus(RequestStatus.COMPLETED);
        assertThrows(BusinessException.class, () -> service.update(illegal));

        ServiceRequest accepted = created.copy();
        accepted.setStatus(RequestStatus.ACCEPTED);
        service.update(accepted);

        ServiceRequest progress = accepted.copy();
        progress.setStatus(RequestStatus.IN_PROGRESS);
        service.update(progress);

        ServiceRequest completed = progress.copy();
        completed.setStatus(RequestStatus.COMPLETED);
        service.update(completed);
        assertNotNull(service.get(created.getId()).getCompletedAt());

        ServiceRequest reopen = service.get(created.getId()).copy();
        reopen.setStatus(RequestStatus.IN_PROGRESS);
        assertThrows(BusinessException.class, () -> service.update(reopen));
    }

    private ServiceRequest validRequest() {
        ServiceRequest request = new ServiceRequest();
        request.setClientId(1L);
        request.setBikeBrand("Trek");
        request.setBikeModel("Marlin 7");
        request.setBikeType(BikeType.MOUNTAIN);
        request.setProblemDescription("Скрипят тормоза");
        request.setEstimatedCost(new BigDecimal("1500.00"));
        return request;
    }

    private static final class InMemoryRequestDao implements ServiceRequestDao {
        private final Map<Long, ServiceRequest> data = new HashMap<>();
        private long sequence = 1;

        @Override public List<ServiceRequest> findAll() { return data.values().stream().map(ServiceRequest::copy).toList(); }
        @Override public Optional<ServiceRequest> findById(long id) {
            return Optional.ofNullable(data.get(id)).map(ServiceRequest::copy);
        }
        @Override public ServiceRequest insert(ServiceRequest request) {
            request.setId(sequence++);
            data.put(request.getId(), request.copy());
            return request;
        }
        @Override public boolean update(ServiceRequest request) {
            if (!data.containsKey(request.getId())) return false;
            data.put(request.getId(), request.copy());
            return true;
        }
        @Override public boolean deleteById(long id) { return data.remove(id) != null; }
    }
}
