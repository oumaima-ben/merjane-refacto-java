package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Order;

public interface OrderService {
    Order processOrder(Long orderId);
}
