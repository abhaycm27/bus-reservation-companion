package com.busreservation.model;

/**
 * Type of seats available on a bus and their corresponding price multiplier.
 */
public enum SeatType {
    WINDOW(1.15),
    AISLE(1.00),
    SLEEPER(1.50);

    private final double priceModifier;

    SeatType(double priceModifier) {
        this.priceModifier = priceModifier;
    }

    public double getPriceModifier() {
        return priceModifier;
    }
}
