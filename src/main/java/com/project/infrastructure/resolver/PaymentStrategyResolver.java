package com.project.infrastructure.resolver;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.project.domain.payment.PaymentStrategy;

@Component
public class PaymentStrategyResolver {

    private final Map<String, PaymentStrategy> strategiesByBeanName;

    public PaymentStrategyResolver(Map<String, PaymentStrategy> strategiesByBeanName) {
        this.strategiesByBeanName = Objects.requireNonNull(strategiesByBeanName, "strategiesByBeanName");
    }

    public PaymentStrategy resolve(String strategyBeanName) {
        if (strategyBeanName == null || strategyBeanName.isBlank()) {
            throw new IllegalArgumentException("Ödeme stratejisi boş olamaz.");
        }
        PaymentStrategy strategy = strategiesByBeanName.get(strategyBeanName);
        if (strategy == null) {
            String supported = strategiesByBeanName.keySet().stream()
                .sorted()
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                "Geçersiz ödeme stratejisi: '" + strategyBeanName + "'. Desteklenenler: " + supported);
        }
        return strategy;
    }
}

