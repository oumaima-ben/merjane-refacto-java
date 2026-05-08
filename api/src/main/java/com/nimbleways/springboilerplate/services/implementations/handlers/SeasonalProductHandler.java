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
public class SeasonalProductHandler implements ProductTypeHandler {

    private final NotificationService notificationService;

    @Override
    public ProductType getHandledType() {
        return ProductType.SEASONAL;
    }

    @Override
    public void process(Product product) {
        LocalDate today = LocalDate.now();
        if (isInSeason(product, today) && product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            return;
        }
        handleSeasonalShortage(product, today);
    }

    private boolean isInSeason(Product product, LocalDate today) {
        return today.isAfter(product.getSeasonStartDate())
                && today.isBefore(product.getSeasonEndDate());
    }

    private void handleSeasonalShortage(Product product, LocalDate today) {
        if (today.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            product.setAvailable(0);
            return;
        }
        if (product.getSeasonStartDate().isAfter(today)) {
            notificationService.sendOutOfStockNotification(product.getName());
            return;
        }
        notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
    }
}
