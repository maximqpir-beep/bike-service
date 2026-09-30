package ru.mirea.bikeservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceRequest {
    private Long id;
    private Long clientId;
    private String bikeBrand;
    private String bikeModel;
    private BikeType bikeType;
    private String problemDescription;
    private RequestStatus status;
    private BigDecimal estimatedCost;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public ServiceRequest() {}
    public ServiceRequest(Long id, Long clientId, String bikeBrand, String bikeModel, BikeType bikeType, String problemDescription, RequestStatus status, BigDecimal estimatedCost, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.clientId = clientId;
        this.bikeBrand = bikeBrand;
        this.bikeModel = bikeModel;
        this.bikeType = bikeType;
        this.problemDescription = problemDescription;
        this.status = status;
        this.estimatedCost = estimatedCost;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getBikeBrand() { return bikeBrand; }
    public void setBikeBrand(String bikeBrand) { this.bikeBrand = bikeBrand; }
    public String getBikeModel() { return bikeModel; }
    public void setBikeModel(String bikeModel) { this.bikeModel = bikeModel; }
    public BikeType getBikeType() { return bikeType; }
    public void setBikeType(BikeType bikeType) { this.bikeType = bikeType; }
    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
