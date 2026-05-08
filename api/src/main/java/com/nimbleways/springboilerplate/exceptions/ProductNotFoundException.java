package com.nimbleways.springboilerplate.exceptions;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(Long productId) {
        super("Product not found: " + productId);
    }
}
