package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void processOrder_returns200_andResponseContainsOrderId() throws Exception {
        Order order = persistOrderWith(List.of(
                normalProduct("USB Cable", 30, 15)
        ));

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));
    }

    @Test
    void processOrder_returns404_withStructuredBody_whenOrderDoesNotExist() throws Exception {
        mockMvc.perform(post("/orders/{orderId}/processOrder", 999_999L)
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found: 999999"))
                .andExpect(jsonPath("$.path").value("/orders/999999/processOrder"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void processOrder_returns400_whenOrderIdIsNotANumber() throws Exception {
        mockMvc.perform(post("/orders/{orderId}/processOrder", "not-a-number")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("orderId")));
    }

    @Test
    void processOrder_returns400_whenOrderIdIsNotPositive() throws Exception {
        mockMvc.perform(post("/orders/{orderId}/processOrder", -1L)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("must be a positive number")));
    }

    @Test
    void processOrder_returns405_whenHttpMethodIsWrong() throws Exception {
        mockMvc.perform(get("/orders/{orderId}/processOrder", 1L))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }

    @Test
    void processOrder_appliesEveryBusinessRule_onMixedOrder() throws Exception {
        LocalDate today = LocalDate.now();

        Product usbCable = normalProduct("USB Cable", 30, 15);
        Product usbDongle = normalProduct("USB Dongle", 0, 10);
        Product butter = expirableProduct("Butter", 30, 15, today.plusDays(26));
        Product milk = expirableProduct("Milk", 6, 90, today.minusDays(2));
        Product watermelon = seasonalProduct("Watermelon", 30, 15,
                today.minusDays(2), today.plusDays(58));
        Product grapes = seasonalProduct("Grapes", 30, 15,
                today.plusDays(180), today.plusDays(240));

        Order order = persistOrderWith(List.of(usbCable, usbDongle, butter, milk, watermelon, grapes));

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        Map<String, Product> reloaded = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getName, Function.identity()));

        assertEquals(29, reloaded.get("USB Cable").getAvailable());
        assertEquals(0, reloaded.get("USB Dongle").getAvailable());
        assertEquals(29, reloaded.get("Butter").getAvailable());
        assertEquals(0, reloaded.get("Milk").getAvailable());
        assertEquals(29, reloaded.get("Watermelon").getAvailable());
        assertEquals(30, reloaded.get("Grapes").getAvailable());

        verify(notificationService, times(1)).sendDelayNotification(10, "USB Dongle");
        verify(notificationService, times(1)).sendExpirationNotification("Milk", milk.getExpiryDate());
        verify(notificationService, times(1)).sendOutOfStockNotification("Grapes");
        verify(notificationService, never()).sendDelayNotification(15, "USB Cable");
        verify(notificationService, never()).sendExpirationNotification("Butter", butter.getExpiryDate());
        verify(notificationService, never()).sendOutOfStockNotification("Watermelon");
    }

    @Test
    void processOrder_seasonalOutOfStockButLeadTimeWithinSeason_sendsDelay() throws Exception {
        LocalDate today = LocalDate.now();
        Product oranges = seasonalProduct("Oranges", 0, 5,
                today.minusDays(10), today.plusDays(60));

        Order order = persistOrderWith(List.of(oranges));

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        Product reloaded = productRepository.findFirstByName("Oranges").orElseThrow();
        assertEquals(0, reloaded.getAvailable());
        verify(notificationService, times(1)).sendDelayNotification(5, "Oranges");
    }

    @Test
    void processOrder_seasonalAfterSeasonEnd_marksUnavailable() throws Exception {
        LocalDate today = LocalDate.now();
        Product strawberries = seasonalProduct("Strawberries", 12, 3,
                today.minusDays(120), today.minusDays(10));

        Order order = persistOrderWith(List.of(strawberries));

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        Product reloaded = productRepository.findFirstByName("Strawberries").orElseThrow();
        assertEquals(0, reloaded.getAvailable());
        verify(notificationService, times(1)).sendOutOfStockNotification("Strawberries");
    }

    private Order persistOrderWith(List<Product> products) {
        productRepository.saveAll(products);
        Order order = new Order();
        order.setItems(new HashSet<>(products));
        return orderRepository.save(order);
    }

    private static Product normalProduct(String name, int available, int leadTime) {
        return Product.builder()
                .type(ProductType.NORMAL).name(name)
                .available(available).leadTime(leadTime)
                .build();
    }

    private static Product expirableProduct(String name, int available, int leadTime, LocalDate expiry) {
        return Product.builder()
                .type(ProductType.EXPIRABLE).name(name)
                .available(available).leadTime(leadTime)
                .expiryDate(expiry)
                .build();
    }

    private static Product seasonalProduct(String name, int available, int leadTime,
                                           LocalDate seasonStart, LocalDate seasonEnd) {
        return Product.builder()
                .type(ProductType.SEASONAL).name(name)
                .available(available).leadTime(leadTime)
                .seasonStartDate(seasonStart).seasonEndDate(seasonEnd)
                .build();
    }
}
