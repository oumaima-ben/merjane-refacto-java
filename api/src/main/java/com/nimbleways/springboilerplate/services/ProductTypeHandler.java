package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;

public interface ProductTypeHandler {

    ProductType getHandledType();

    void process(Product product);
}
