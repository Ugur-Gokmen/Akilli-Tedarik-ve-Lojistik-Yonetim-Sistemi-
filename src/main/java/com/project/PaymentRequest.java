package com.project.api.dto;

import java.util.Map;

public record PaymentRequest(
    String orderId,
    String strategyBeanName, // Örn: "creditCardStrategy", "cryptoStrategy"
    Map<String, String> details
) {}
