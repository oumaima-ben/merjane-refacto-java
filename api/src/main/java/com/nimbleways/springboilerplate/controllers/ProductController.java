package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.dto.product.ProductAvailabilityResponse;
import com.nimbleways.springboilerplate.services.ProductAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductAvailabilityService availabilityService;

    @GetMapping("/{productId}/availability")
    public ResponseEntity<ProductAvailabilityResponse> getAvailability(
            @PathVariable @Positive(message = "productId must be a positive number") Long productId) {
        return ResponseEntity.ok(availabilityService.checkAvailability(productId));
    }
}
