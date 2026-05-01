package com.project.domain.payment;


import com.project.domain.order.Order;
import com.project.infrastructure.logger.SystemLogger;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Kredi kartı ödeme stratejisi - Strategy Pattern concrete implementasyonu.
 *
 * <p>Spring IoC ile Stateless Singleton Bean olarak çalışır.</p>
 */
@Component("creditCardStrategy")
public class CreditCardPayment implements PaymentStrategy {

    private static final SystemLogger logger = SystemLogger.getInstance();

    @Override
    public PaymentResult pay(Order order, double amount, Map<String, String> details) {
        String rawCardNumber = details.getOrDefault("cardNumber", "0000000000000000");
        String maskedCard = "**** **** **** " + rawCardNumber.substring(Math.max(0, rawCardNumber.length() - 4));

        logger.logCriticalOperation("PAYMENT",
            String.format("Kredi Kartı ödemesi başlatıldı | Sipariş: %s | Tutar: %.2f TL | Kart: %s",
                order.getId(), amount, maskedCard));
        String transactionId = "CC-" + System.currentTimeMillis();
        logger.logCriticalOperation("PAYMENT_SUCCESS",
            String.format("Kredi kartı ödemesi başarılı | Transaction: %s", transactionId));
        return PaymentResult.success(transactionId, getMethodName(), amount);
    }

    @Override
    public String getMethodName() { return "Kredi Kartı"; }
}
