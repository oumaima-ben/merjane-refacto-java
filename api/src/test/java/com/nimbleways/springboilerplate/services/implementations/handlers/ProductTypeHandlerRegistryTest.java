package com.nimbleways.springboilerplate.services.implementations.handlers;

import com.nimbleways.springboilerplate.dto.product.AvailabilityReason;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.ProductTypeHandler;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
class ProductTypeHandlerRegistryTest {

    @Test
    void getHandler_returnsHandlerForRegisteredType() {
        ProductTypeHandler normalHandler = new StubHandler(ProductType.NORMAL);
        ProductTypeHandler seasonalHandler = new StubHandler(ProductType.SEASONAL);
        ProductTypeHandlerRegistry registry =
                new ProductTypeHandlerRegistry(List.of(normalHandler, seasonalHandler));

        assertEquals(normalHandler, registry.getHandler(ProductType.NORMAL));
        assertEquals(seasonalHandler, registry.getHandler(ProductType.SEASONAL));
    }

    @Test
    void getHandler_throws_whenTypeNotRegistered() {
        ProductTypeHandlerRegistry registry =
                new ProductTypeHandlerRegistry(List.of(new StubHandler(ProductType.NORMAL)));

        assertThrows(IllegalStateException.class, () -> registry.getHandler(ProductType.SEASONAL));
    }

    private static final class StubHandler implements ProductTypeHandler {
        private final ProductType type;

        StubHandler(ProductType type) {
            this.type = type;
        }

        @Override
        public ProductType getHandledType() {
            return type;
        }

        @Override
        public void process(Product product) {
        }

        @Override
        public AvailabilityReason checkAvailability(Product product) {
            return AvailabilityReason.AVAILABLE;
        }
    }
}
