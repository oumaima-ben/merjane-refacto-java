package com.nimbleways.springboilerplate.services.implementations.handlers;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@UnitTest
@ExtendWith(MockitoExtension.class)
class NormalProductHandlerTest {

    @Mock
    private NotificationService notificationService;

    private NormalProductHandler handler;

    @BeforeEach
    void setUp() {
        handler = new NormalProductHandler(notificationService);
    }

    @Test
    void getHandledType_returnsNormal() {
        assertEquals(ProductType.NORMAL, handler.getHandledType());
    }

    @Test
    void process_decrementsAvailable_whenInStock() {
        Product product = Product.builder()
                .leadTime(10).available(5).type(ProductType.NORMAL).name("USB Cable")
                .build();

        handler.process(product);

        assertEquals(4, product.getAvailable());
        verifyNoInteractions(notificationService);
    }

    @Test
    void process_sendsDelayNotification_whenOutOfStockWithLeadTime() {
        Product product = Product.builder()
                .leadTime(15).available(0).type(ProductType.NORMAL).name("USB Dongle")
                .build();

        handler.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService).sendDelayNotification(15, "USB Dongle");
    }

    @Test
    void process_doesNothing_whenOutOfStockWithoutLeadTime() {
        Product product = Product.builder()
                .leadTime(0).available(0).type(ProductType.NORMAL).name("Adapter")
                .build();

        handler.process(product);

        assertEquals(0, product.getAvailable());
        verifyNoInteractions(notificationService);
    }
}
