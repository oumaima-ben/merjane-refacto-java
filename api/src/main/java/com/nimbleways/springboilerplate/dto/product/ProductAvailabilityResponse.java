package com.nimbleways.springboilerplate.dto.product;

import com.nimbleways.springboilerplate.entities.ProductType;

public record ProductAvailabilityResponse(
        Long productId,
        String name,
        ProductType type,
        boolean available,
        AvailabilityReason reason
) {
}
