package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

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
    void availability_normalInStock_returnsAvailable() throws Exception {
        Product saved = productRepository.save(Product.builder()
                .type(ProductType.NORMAL).name("USB Cable")
                .available(30).leadTime(15).build());

        mockMvc.perform(get("/products/{id}/availability", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.reason").value("AVAILABLE"))
                .andExpect(jsonPath("$.type").value("NORMAL"));
    }

    @Test
    void availability_normalEmpty_returnsOutOfStock() throws Exception {
        Product saved = productRepository.save(Product.builder()
                .type(ProductType.NORMAL).name("USB Dongle")
                .available(0).leadTime(10).build());

        mockMvc.perform(get("/products/{id}/availability", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.reason").value("OUT_OF_STOCK"));
    }

    @Test
    void availability_seasonalInSeason_returnsAvailable() throws Exception {
        LocalDate today = LocalDate.now();
        Product saved = productRepository.save(Product.builder()
                .type(ProductType.SEASONAL).name("Watermelon")
                .available(30).leadTime(15)
                .seasonStartDate(today.minusDays(2)).seasonEndDate(today.plusDays(58))
                .build());

        mockMvc.perform(get("/products/{id}/availability", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.reason").value("AVAILABLE"));
    }

    @Test
    void availability_seasonalBeforeSeason_returnsOutOfSeason() throws Exception {
        LocalDate today = LocalDate.now();
        Product saved = productRepository.save(Product.builder()
                .type(ProductType.SEASONAL).name("Grapes")
                .available(30).leadTime(15)
                .seasonStartDate(today.plusDays(180)).seasonEndDate(today.plusDays(240))
                .build());

        mockMvc.perform(get("/products/{id}/availability", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.reason").value("OUT_OF_SEASON"));
    }

    @Test
    void availability_expirableExpired_returnsExpired() throws Exception {
        Product saved = productRepository.save(Product.builder()
                .type(ProductType.EXPIRABLE).name("Milk")
                .available(6).leadTime(90)
                .expiryDate(LocalDate.now().minusDays(2))
                .build());

        mockMvc.perform(get("/products/{id}/availability", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.reason").value("EXPIRED"));
    }

    @Test
    void availability_unknownProduct_returns404() throws Exception {
        mockMvc.perform(get("/products/{id}/availability", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found: 999999"));
    }

    @Test
    void availability_negativeId_returns400() throws Exception {
        mockMvc.perform(get("/products/{id}/availability", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
