package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductTypeHandler;
import com.nimbleways.springboilerplate.services.implementations.handlers.ProductTypeHandlerRegistry;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductTypeHandlerRegistry handlerRegistry;
    @Mock
    private ProductTypeHandler normalHandler;
    @Mock
    private ProductTypeHandler seasonalHandler;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, productRepository, handlerRegistry);
    }

    @Test
    void processOrder_throwsOrderNotFound_whenIdDoesNotExist() {
        when(orderRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.processOrder(42L));
        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void processOrder_dispatchesEachItemToItsHandlerAndPersistsAll() {
        Product normal = Product.builder().id(1L).type(ProductType.NORMAL).build();
        Product seasonal = Product.builder().id(2L).type(ProductType.SEASONAL).build();
        Set<Product> items = new HashSet<>(Set.of(normal, seasonal));
        Order order = new Order();
        order.setId(7L);
        order.setItems(items);

        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(handlerRegistry.getHandler(ProductType.NORMAL)).thenReturn(normalHandler);
        when(handlerRegistry.getHandler(ProductType.SEASONAL)).thenReturn(seasonalHandler);

        Order result = orderService.processOrder(7L);

        assertEquals(7L, result.getId());
        verify(normalHandler, times(1)).process(normal);
        verify(seasonalHandler, times(1)).process(seasonal);
        verify(productRepository, times(1)).saveAll(items);
    }
}
