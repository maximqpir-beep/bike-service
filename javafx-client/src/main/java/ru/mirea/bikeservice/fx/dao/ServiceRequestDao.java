package ru.mirea.bikeservice.fx.dao;

import ru.mirea.bikeservice.fx.model.ServiceRequest;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestDao {
    List<ServiceRequest> findAll();
    Optional<ServiceRequest> findById(long id);
    ServiceRequest insert(ServiceRequest request);
    boolean update(ServiceRequest request);
    boolean deleteById(long id);
}
