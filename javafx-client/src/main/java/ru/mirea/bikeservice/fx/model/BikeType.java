package ru.mirea.bikeservice.fx.model;

public enum BikeType {
    ROAD("Шоссейный"),
    MOUNTAIN("Горный"),
    CITY("Городской"),
    BMX("BMX"),
    ELECTRIC("Электровелосипед"),
    OTHER("Другой");

    private final String title;

    BikeType(String title) { this.title = title; }

    @Override public String toString() { return title; }
}
