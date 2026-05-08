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
class ExpirableProductHandlerTest {

    @Mock
    private NotificationService notificationService;

    private ExpirableProductHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ExpirableProductHandler(notificationService);
    }

    @Test
    void getHandledType_returnsExpirable() {
        assertEquals(ProductType.EXPIRABLE, handler.getHandledType());
    }

    @Test
    void process_decrementsAvailable_whenInStockAndNotExpired() {
        LocalDate expiry = LocalDate.now().plusDays(30);
        Product product = Product.builder()
                .leadTime(15).available(20).type(ProductType.EXPIRABLE).name("Butter")
                .expiryDate(expiry)
                .build();

        handler.process(product);

        assertEquals(19, product.getAvailable());
        verifyNoInteractions(notificationService);
    }

    @Test
    void process_notifiesAndZeroesOut_whenExpired() {
        LocalDate expiry = LocalDate.now().minusDays(2);
        Product product = Product.builder()
                .leadTime(90).available(6).type(ProductType.EXPIRABLE).name("Milk")
                .expiryDate(expiry)
                .build();

        handler.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService).sendExpirationNotification("Milk", expiry);
    }

    @Test
    void process_notifiesAndZeroesOut_whenOutOfStockEvenIfNotExpired() {
        LocalDate expiry = LocalDate.now().plusDays(30);
        Product product = Product.builder()
                .leadTime(15).available(0).type(ProductType.EXPIRABLE).name("Yogurt")
                .expiryDate(expiry)
                .build();

        handler.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService).sendExpirationNotification("Yogurt", expiry);
    }

    @Test
    void checkAvailability_returnsAvailable_whenInStockAndNotExpired() {
        Product product = Product.builder()
                .available(10).type(ProductType.EXPIRABLE).name("Butter")
                .expiryDate(LocalDate.now().plusDays(26))
                .build();

        assertEquals(AvailabilityReason.AVAILABLE, handler.checkAvailability(product));
    }

    @Test
    void checkAvailability_returnsExpired_whenPastExpiry() {
        Product product = Product.builder()
                .available(6).type(ProductType.EXPIRABLE).name("Milk")
                .expiryDate(LocalDate.now().minusDays(2))
                .build();

        assertEquals(AvailabilityReason.EXPIRED, handler.checkAvailability(product));
    }

    @Test
    void checkAvailability_returnsOutOfStock_whenEmptyButNotExpired() {
        Product product = Product.builder()
                .available(0).type(ProductType.EXPIRABLE).name("Yogurt")
                .expiryDate(LocalDate.now().plusDays(15))
                .build();

        assertEquals(AvailabilityReason.OUT_OF_STOCK, handler.checkAvailability(product));
    }
}
