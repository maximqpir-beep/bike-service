package ru.mirea.bikeservice.service;

import ru.mirea.bikeservice.exception.BusinessException;
public final class Validation {
    private Validation() {}
    public static String required(String value, String name, int max) {
        if (value == null || value.isBlank()) throw new BusinessException(name + ": значение обязательно.");
        value = value.trim();
        if (value.length() > max) throw new BusinessException(name + ": максимум " + max + " символов.");
        return value;
    }
}
