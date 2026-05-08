package com.nimbleways.springboilerplate.services.implementations.handlers;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.ProductTypeHandler;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ExpirableProductHandler implements ProductTypeHandler {

    private final NotificationService notificationService;

    @Override
    public ProductType getHandledType() {
        return ProductType.EXPIRABLE;
    }

    @Override
    public void process(Product product) {
        if (isSellable(product)) {
            product.setAvailable(product.getAvailable() - 1);
            return;
        }
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
    }

    private boolean isSellable(Product product) {
        return product.getAvailable() > 0
                && product.getExpiryDate().isAfter(LocalDate.now());
    }
}
