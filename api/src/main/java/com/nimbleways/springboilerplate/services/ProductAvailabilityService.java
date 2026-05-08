package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.dto.product.ProductAvailabilityResponse;

public interface ProductAvailabilityService {
    ProductAvailabilityResponse checkAvailability(Long productId);
}
