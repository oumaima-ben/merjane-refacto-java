package com.nimbleways.springboilerplate.services.implementations.handlers;

import com.nimbleways.springboilerplate.dto.product.AvailabilityReason;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SeasonalProductHandlerTest {

    @Mock
    private NotificationService notificationService;

    private SeasonalProductHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SeasonalProductHandler(notificationService);
    }

    @Test
    void getHandledType_returnsSeasonal() {
        assertEquals(ProductType.SEASONAL, handler.getHandledType());
    }

    @Test
    void process_decrementsAvailable_whenInSeasonAndInStock() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .leadTime(15).available(10).type(ProductType.SEASONAL).name("Watermelon")
                .seasonStartDate(today.minusDays(2))
                .seasonEndDate(today.plusDays(58))
                .build();

        handler.process(product);

        assertEquals(9, product.getAvailable());
        verifyNoInteractions(notificationService);
    }

    @Test
    void process_marksUnavailable_whenLeadTimeExceedsSeasonEnd() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .leadTime(30).available(0).type(ProductType.SEASONAL).name("Watermelon")
                .seasonStartDate(today.minusDays(60))
                .seasonEndDate(today.plusDays(5))
                .build();

        handler.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService).sendOutOfStockNotification("Watermelon");
    }

    @Test
    void process_sendsOutOfStock_whenBeforeSeasonStarts() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .leadTime(15).available(30).type(ProductType.SEASONAL).name("Grapes")
                .seasonStartDate(today.plusDays(180))
                .seasonEndDate(today.plusDays(240))
                .build();

        handler.process(product);

        assertEquals(30, product.getAvailable());
        verify(notificationService).sendOutOfStockNotification("Grapes");
    }

    @Test
    void process_sendsDelayNotification_whenInSeasonOutOfStockButLeadTimeFitsInSeason() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .leadTime(5).available(0).type(ProductType.SEASONAL).name("Watermelon")
                .seasonStartDate(today.minusDays(10))
                .seasonEndDate(today.plusDays(60))
                .build();

        handler.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService).sendDelayNotification(5, "Watermelon");
    }

    @Test
    void process_marksUnavailable_whenSeasonAlreadyOver() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .leadTime(2).available(8).type(ProductType.SEASONAL).name("Watermelon")
                .seasonStartDate(today.minusDays(120))
                .seasonEndDate(today.minusDays(10))
                .build();

        handler.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService).sendOutOfStockNotification("Watermelon");
    }

    @Test
    void checkAvailability_returnsAvailable_whenInSeasonAndInStock() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .available(10).type(ProductType.SEASONAL).name("Watermelon")
                .seasonStartDate(today.minusDays(2))
                .seasonEndDate(today.plusDays(58))
                .build();

        assertEquals(AvailabilityReason.AVAILABLE, handler.checkAvailability(product));
    }

    @Test
    void checkAvailability_returnsOutOfStock_whenInSeasonButEmpty() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .available(0).type(ProductType.SEASONAL).name("Watermelon")
                .seasonStartDate(today.minusDays(2))
                .seasonEndDate(today.plusDays(58))
                .build();

        assertEquals(AvailabilityReason.OUT_OF_STOCK, handler.checkAvailability(product));
    }

    @Test
    void checkAvailability_returnsOutOfSeason_whenBeforeSeason() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .available(30).type(ProductType.SEASONAL).name("Grapes")
                .seasonStartDate(today.plusDays(180))
                .seasonEndDate(today.plusDays(240))
                .build();

        assertEquals(AvailabilityReason.OUT_OF_SEASON, handler.checkAvailability(product));
    }
}
