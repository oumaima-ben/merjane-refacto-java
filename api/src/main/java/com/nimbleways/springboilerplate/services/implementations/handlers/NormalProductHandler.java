package com.nimbleways.springboilerplate.services.implementations.handlers;

import com.nimbleways.springboilerplate.dto.product.AvailabilityReason;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.ProductTypeHandler;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NormalProductHandler implements ProductTypeHandler {

    private final NotificationService notificationService;

    @Override
    public ProductType getHandledType() {
        return ProductType.NORMAL;
    }

    @Override
    public void process(Product product) {
        if (product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            return;
        }
        if (product.getLeadTime() > 0) {
            notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
        }
    }

    @Override
    public AvailabilityReason checkAvailability(Product product) {
        return product.getAvailable() > 0
                ? AvailabilityReason.AVAILABLE
                : AvailabilityReason.OUT_OF_STOCK;
    }
}
