package com.project.domain.payment;

import com.project.domain.order.Order;

import java.util.Map;

/**
 * Ödeme stratejisi arayüzü - Strategy Pattern (Stateless).
 */
public interface PaymentStrategy {
    PaymentResult pay(Order order, double amount, Map<String, String> details);
    String getMethodName();
}
