package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;

/**
 * Strategy contract for processing a single {@link Product}.
 * <p>
 * Each {@link ProductType} has exactly one implementation, picked at
 * runtime by {@code ProductTypeHandlerRegistry}.
 */
public interface ProductTypeHandler {

    /** @return the product type this handler is responsible for. */
    ProductType getHandledType();

    /**
     * Apply the type-specific business rule (decrement stock, notify, etc.).
     * The product is mutated in place; persistence is the caller's job.
     */
    void process(Product product);
}
