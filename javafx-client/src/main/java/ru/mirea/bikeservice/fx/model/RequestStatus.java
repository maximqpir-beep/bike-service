package ru.mirea.bikeservice.fx.model;

public enum RequestStatus {
    CREATED("Создана"),
    ACCEPTED("Принята"),
    IN_PROGRESS("В работе"),
    WAITING_FOR_PARTS("Ожидает запчасти"),
    COMPLETED("Завершена"),
    CANCELLED("Отменена");

    private final String title;

    RequestStatus(String title) { this.title = title; }

    @Override public String toString() { return title; }
}
