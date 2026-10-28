package ru.mirea.bikeservice.fx.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import ru.mirea.bikeservice.fx.AppContext;
import ru.mirea.bikeservice.fx.export.ExcelExporter;
import ru.mirea.bikeservice.fx.model.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class MainController {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.of("ru", "RU"));

    @FXML private TextField searchField;
    @FXML private ComboBox<RequestStatus> statusFilter;
    @FXML private ComboBox<BikeType> typeFilter;
    @FXML private ComboBox<String> sortBox;
    @FXML private ComboBox<String> directionBox;
    @FXML private TableView<RequestRow> requestTable;
    @FXML private TableColumn<RequestRow, Long> idColumn;
    @FXML private TableColumn<RequestRow, String> clientColumn;
    @FXML private TableColumn<RequestRow, String> phoneColumn;
    @FXML private TableColumn<RequestRow, String> bikeColumn;
    @FXML private TableColumn<RequestRow, BikeType> typeColumn;
    @FXML private TableColumn<RequestRow, String> problemColumn;
    @FXML private TableColumn<RequestRow, RequestStatus> statusColumn;
    @FXML private TableColumn<RequestRow, BigDecimal> costColumn;
    @FXML private TableColumn<RequestRow, LocalDateTime> createdColumn;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label statusLabel;

    @FXML private Label clientsStat;
    @FXML private Label requestsStat;
    @FXML private Label activeStat;
    @FXML private Label waitingStat;
    @FXML private Label completedStat;
    @FXML private Label cancelledStat;
    @FXML private Label averageCostStat;
    @FXML private Label revenueStat;

    private final AppContext context = AppContext.get();
    private final ObservableList<RequestRow> source = FXCollections.observableArrayList();
    private final FilteredList<RequestRow> filtered = new FilteredList<>(source, ignored -> true);
    private final SortedList<RequestRow> sorted = new SortedList<>(filtered);
    private List<Client> clients = List.of();

    @FXML
    private void initialize() {
        configureTable();
        statusFilter.getItems().setAll(RequestStatus.values());
        typeFilter.getItems().setAll(BikeType.values());
        sortBox.getItems().setAll("ID", "Клиент", "Стоимость", "Дата создания");
        directionBox.getItems().setAll("По возрастанию", "По убыванию");
        sortBox.setValue("Дата создания");
        directionBox.setValue("По убыванию");

        sorted.comparatorProperty().bind(requestTable.comparatorProperty());
        requestTable.setItems(sorted);
        requestTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            editButton.setDisable(selected == null);
            deleteButton.setDisable(selected == null);
        });
        searchField.textProperty().addListener((obs, old, value) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, old, value) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, old, value) -> applyFilters());
        refreshData();
        applySort();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().id()));
        clientColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().clientName()));
        phoneColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().phone()));
        bikeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().bike()));
        typeColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().request().getBikeType()));
        problemColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().request().getProblemDescription()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().request().getStatus()));
        costColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().request().getEstimatedCost()));
        createdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().request().getCreatedAt()));
        costColumn.setCellFactory(textCell(value -> value == null ? "—" : MONEY.format(value)));
        createdColumn.setCellFactory(textCell(value -> value == null ? "—" : DATE_TIME.format(value)));
        problemColumn.setCellFactory(column -> {
            TableCell<RequestRow, String> cell = new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setTooltip(empty || item == null ? null : new Tooltip(item));
                }
            };
            return cell;
        });
    }

    private <T> Callback<TableColumn<RequestRow, T>, TableCell<RequestRow, T>> textCell(java.util.function.Function<T, String> formatter) {
        return ignored -> new TableCell<>() {
            @Override protected void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : formatter.apply(value));
            }
        };
    }

    @FXML
    private void refreshData() {
        try {
            clients = context.clients().findAll();
            Map<Long, Client> clientsById = new HashMap<>();
            clients.forEach(client -> clientsById.put(client.getId(), client));
            List<ServiceRequest> requests = context.requests().findAll();
            source.setAll(requests.stream()
                    .map(request -> new RequestRow(request, clientsById.get(request.getClientId())))
                    .toList());
            updateStatistics(requests);
            statusLabel.setText("Загружено заявок: " + requests.size());
        } catch (RuntimeException error) {
            statusLabel.setText("Не удалось обновить данные");
            Alerts.error(requestTable.getScene() == null ? null : requestTable.getScene().getWindow(), readable(error));
        }
    }

    @FXML
    private void addRequest() {
        showRequestDialog("Новая заявка", null);
    }

    @FXML
    private void editRequest() {
        RequestRow selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        RequestStatus status = selected.request().getStatus();
        if (status == RequestStatus.COMPLETED || status == RequestStatus.CANCELLED) {
            Alerts.error(requestTable.getScene().getWindow(), "Завершённую или отменённую заявку нельзя редактировать.");
            return;
        }
        showRequestDialog("Редактирование заявки №" + selected.id(), selected.request());
    }

    @FXML
    private void deleteRequest() {
        RequestRow selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        boolean confirmed = Alerts.confirm(requestTable.getScene().getWindow(),
                "Удалить заявку №" + selected.id() + "?",
                selected.clientName() + " · " + selected.bike() + "\nДействие нельзя отменить.");
        if (!confirmed) return;
        try {
            context.requests().delete(selected.id());
            refreshData();
            statusLabel.setText("Заявка №" + selected.id() + " удалена");
        } catch (RuntimeException error) {
            Alerts.error(requestTable.getScene().getWindow(), readable(error));
        }
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        statusFilter.getSelectionModel().clearSelection();
        typeFilter.getSelectionModel().clearSelection();
        applyFilters();
    }

    @FXML
    private void applySort() {
        TableColumn<RequestRow, ?> column = switch (sortBox.getValue() == null ? "" : sortBox.getValue()) {
            case "Клиент" -> clientColumn;
            case "Стоимость" -> costColumn;
            case "Дата создания" -> createdColumn;
            default -> idColumn;
        };
        column.setSortType("По убыванию".equals(directionBox.getValue())
                ? TableColumn.SortType.DESCENDING : TableColumn.SortType.ASCENDING);
        requestTable.getSortOrder().setAll(column);
        requestTable.sort();
    }

    @FXML
    private void exportCurrentView() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Экспорт текущего списка заявок");
        chooser.setInitialFileName("bike-service-requests.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        File target = chooser.showSaveDialog(requestTable.getScene().getWindow());
        if (target == null) return;
        try {
            new ExcelExporter().export(target.toPath(), List.copyOf(sorted));
            statusLabel.setText("Экспортировано строк: " + sorted.size() + " · " + target.getName());
        } catch (IOException error) {
            Alerts.error(requestTable.getScene().getWindow(), "Не удалось сохранить Excel: " + error.getMessage());
        }
    }

    private void showRequestDialog(String title, ServiceRequest request) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/request-dialog.fxml"));
            DialogPane pane = loader.load();
            Dialog<ServiceRequest> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.initOwner(requestTable.getScene().getWindow());
            dialog.setDialogPane(pane);
            loader.<RequestDialogController>getController()
                    .configure(dialog, context.requests(), clients, request);
            if (dialog.showAndWait().isPresent()) {
                refreshData();
                statusLabel.setText(request == null ? "Заявка создана" : "Заявка изменена");
            }
        } catch (IOException error) {
            Alerts.error(requestTable.getScene().getWindow(), "Не удалось открыть форму: " + error.getMessage());
        }
    }

    private void applyFilters() {
        String query = Optional.ofNullable(searchField.getText()).orElse("").trim().toLowerCase(Locale.ROOT);
        RequestStatus status = statusFilter.getValue();
        BikeType type = typeFilter.getValue();
        filtered.setPredicate(row -> {
            ServiceRequest request = row.request();
            boolean matchesQuery = query.isBlank() || String.join(" ",
                            row.clientName(), row.phone(), request.getBikeBrand(),
                            Optional.ofNullable(request.getBikeModel()).orElse(""),
                            request.getProblemDescription())
                    .toLowerCase(Locale.ROOT).contains(query);
            return matchesQuery && (status == null || request.getStatus() == status)
                    && (type == null || request.getBikeType() == type);
        });
        statusLabel.setText("Показано: " + filtered.size() + " из " + source.size());
    }

    private void updateStatistics(List<ServiceRequest> requests) {
        Statistics value = context.statistics().calculate(clients.size(), requests);
        clientsStat.setText(String.valueOf(value.clients()));
        requestsStat.setText(String.valueOf(value.requests()));
        activeStat.setText(String.valueOf(value.active()));
        waitingStat.setText(String.valueOf(value.waiting()));
        completedStat.setText(String.valueOf(value.completed()));
        cancelledStat.setText(String.valueOf(value.cancelled()));
        averageCostStat.setText(MONEY.format(value.averageCost()));
        revenueStat.setText(MONEY.format(value.completedRevenue()));
    }

    private String readable(RuntimeException error) {
        return error.getMessage() == null || error.getMessage().isBlank()
                ? "Неизвестная ошибка." : error.getMessage();
    }
}
