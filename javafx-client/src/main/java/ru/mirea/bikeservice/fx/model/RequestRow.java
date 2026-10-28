package ru.mirea.bikeservice.fx.model;

public record RequestRow(ServiceRequest request, Client client) {
    public Long id() { return request.getId(); }
    public String clientName() { return client == null ? "Клиент не найден" : client.getFullName(); }
    public String phone() { return client == null ? "" : client.getPhone(); }
    public String bike() {
        String model = request.getBikeModel();
        return request.getBikeBrand() + (model == null || model.isBlank() ? "" : " " + model);
    }
}
