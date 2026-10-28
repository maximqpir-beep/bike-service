package ru.mirea.bikeservice.fx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.mirea.bikeservice.fx.exception.BusinessException;
import ru.mirea.bikeservice.fx.model.*;
import ru.mirea.bikeservice.fx.service.ServiceRequestService;

import java.math.BigDecimal;
import java.util.List;

public final class RequestDialogController {
    @FXML private DialogPane dialogPane;
    @FXML private ComboBox<Client> clientBox;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private ComboBox<BikeType> bikeTypeBox;
    @FXML private TextArea problemArea;
    @FXML private TextField costField;
    @FXML private ComboBox<RequestStatus> statusBox;
    @FXML private Label statusHint;

    private ServiceRequestService service;
    private ServiceRequest original;
    private ServiceRequest saved;

    @FXML
    private void initialize() {
        bikeTypeBox.getItems().setAll(BikeType.values());
    }

    public void configure(Dialog<ServiceRequest> dialog,
                          ServiceRequestService service,
                          List<Client> clients,
                          ServiceRequest original) {
        this.service = service;
        this.original = original;
        clientBox.getItems().setAll(clients);

        if (original == null) {
            statusBox.getItems().setAll(RequestStatus.CREATED);
            statusBox.setValue(RequestStatus.CREATED);
            statusBox.setDisable(true);
            statusHint.setText("Новая заявка всегда создаётся со статусом «Создана».");
            if (!clients.isEmpty()) clientBox.setValue(clients.getFirst());
        } else {
            fill(original, clients);
            statusBox.getItems().setAll(service.allowedStatuses(original.getStatus()));
            statusBox.setValue(original.getStatus());
            statusHint.setText("Доступны только разрешённые переходы статуса.");
        }

        Button saveButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        saveButton.setText("Сохранить");
        ((Button) dialogPane.lookupButton(ButtonType.CANCEL)).setText("Отмена");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                ServiceRequest value = buildRequest();
                if (original == null) saved = service.create(value);
                else {
                    service.update(value);
                    saved = value;
                }
            } catch (RuntimeException error) {
                event.consume();
                Alerts.error(dialogPane.getScene().getWindow(), readable(error));
            }
        });
        dialog.setResultConverter(button -> button == ButtonType.OK ? saved : null);
    }

    private void fill(ServiceRequest request, List<Client> clients) {
        clients.stream().filter(client -> client.getId().equals(request.getClientId())).findFirst()
                .ifPresent(clientBox::setValue);
        brandField.setText(request.getBikeBrand());
        modelField.setText(request.getBikeModel());
        bikeTypeBox.setValue(request.getBikeType());
        problemArea.setText(request.getProblemDescription());
        costField.setText(request.getEstimatedCost() == null ? "" : request.getEstimatedCost().toPlainString());
    }

    private ServiceRequest buildRequest() {
        Client client = clientBox.getValue();
        if (client == null) throw new BusinessException("Выберите клиента.");
        ServiceRequest value = original == null ? new ServiceRequest() : original.copy();
        value.setClientId(client.getId());
        value.setBikeBrand(brandField.getText());
        value.setBikeModel(modelField.getText());
        value.setBikeType(bikeTypeBox.getValue());
        value.setProblemDescription(problemArea.getText());
        value.setEstimatedCost(parseCost(costField.getText()));
        value.setStatus(statusBox.getValue());
        return value;
    }

    private BigDecimal parseCost(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return new BigDecimal(text.trim().replace(',', '.'));
        } catch (NumberFormatException error) {
            throw new BusinessException("Стоимость должна быть числом, например 1500.50.");
        }
    }

    private String readable(RuntimeException error) {
        return error.getMessage() == null || error.getMessage().isBlank()
                ? "Неизвестная ошибка." : error.getMessage();
    }
}
