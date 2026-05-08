package com.nimbleways.springboilerplate.dto.product;

public enum AvailabilityReason {
    AVAILABLE(true),
    OUT_OF_STOCK(false),
    OUT_OF_SEASON(false),
    EXPIRED(false);

    private final boolean available;

    AvailabilityReason(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }
}
