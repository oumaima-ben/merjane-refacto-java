package com.nimbleways.springboilerplate.services.implementations.handlers;

import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.ProductTypeHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProductTypeHandlerRegistry {

    private final Map<ProductType, ProductTypeHandler> handlersByType;

    public ProductTypeHandlerRegistry(List<ProductTypeHandler> handlers) {
        this.handlersByType = handlers.stream()
                .collect(Collectors.toMap(ProductTypeHandler::getHandledType, Function.identity()));
    }

    public ProductTypeHandler getHandler(ProductType type) {
        ProductTypeHandler handler = handlersByType.get(type);
        if (handler == null) {
            throw new IllegalStateException("No handler registered for product type: " + type);
        }
        return handler;
    }
}
