package ru.mirea.bikeservice.fx.model;

import java.math.BigDecimal;

public record Statistics(
        int clients,
        int requests,
        long active,
        long waiting,
        long completed,
        long cancelled,
        BigDecimal averageCost,
        BigDecimal completedRevenue) {}
