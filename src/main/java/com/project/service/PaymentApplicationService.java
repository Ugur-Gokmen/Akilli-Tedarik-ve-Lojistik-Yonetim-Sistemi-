package com.project.service;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.project.domain.payment.PaymentResult;
import com.project.domain.payment.PaymentStrategy;
import com.project.infrastructure.resolver.PaymentStrategyResolver;

/**
 * Web/REST katmanları için ödeme orchestration servisi.
 *
 * <p>Controller katmanında bean çözümleme ve OrderService çağrılarını
 * tek bir noktada toplar.</p>
 */
@Service
public class PaymentApplicationService {

    private final OrderService orderService;
    private final PaymentStrategyResolver paymentStrategyResolver;

    public PaymentApplicationService(OrderService orderService, PaymentStrategyResolver paymentStrategyResolver) {
        this.orderService = Objects.requireNonNull(orderService, "orderService");
        this.paymentStrategyResolver = Objects.requireNonNull(paymentStrategyResolver, "paymentStrategyResolver");
    }

    public PaymentResult processPayment(String orderId, String strategyBeanName, Map<String, String> details) {
        PaymentStrategy strategy = paymentStrategyResolver.resolve(strategyBeanName);
        Map<String, String> safeDetails = (details == null) ? Map.of() : details;
        return orderService.pay(orderId, strategy, safeDetails);
    }
}

