package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.AvailabilityReason;
import com.nimbleways.springboilerplate.dto.product.ProductAvailabilityResponse;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.ProductNotFoundException;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductAvailabilityService;
import com.nimbleways.springboilerplate.services.implementations.handlers.ProductTypeHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductAvailabilityServiceImpl implements ProductAvailabilityService {

    private final ProductRepository productRepository;
    private final ProductTypeHandlerRegistry handlerRegistry;

    @Override
    @Transactional(readOnly = true)
    public ProductAvailabilityResponse checkAvailability(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        AvailabilityReason reason = handlerRegistry.getHandler(product.getType()).checkAvailability(product);
        return new ProductAvailabilityResponse(
                product.getId(),
                product.getName(),
                product.getType(),
                reason.isAvailable(),
                reason
        );
    }
}
