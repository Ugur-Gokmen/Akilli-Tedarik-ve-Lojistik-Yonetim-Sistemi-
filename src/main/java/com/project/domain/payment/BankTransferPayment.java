package com.project.domain.payment;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.project.domain.order.Order;
import com.project.infrastructure.logger.SystemLogger;

/**
 * Banka havalesi / EFT ödeme stratejisi - Strategy Pattern concrete implementasyonu.
 */
@Component("bankTransferStrategy")
public class BankTransferPayment implements PaymentStrategy {

    private static final SystemLogger logger = SystemLogger.getInstance();

    @Override
    public PaymentResult pay(Order order, double amount, Map<String, String> details) {
        String bankName = details.getOrDefault("bankName", "Bilinmeyen Banka");
        String iban = details.getOrDefault("iban", "TR000000000000000000000000");
        logger.logCriticalOperation("PAYMENT",
            String.format("Havale ödemesi başlatıldı | Sipariş: %s | Tutar: %.2f TL | Banka: %s | IBAN: %s",
                order.getId(), amount, bankName, iban));
        String transactionId = "BT-" + System.currentTimeMillis();
        logger.logCriticalOperation("PAYMENT_SUCCESS",
            String.format("Havale ödemesi alındı | Transaction: %s | 1-3 iş günü içinde onaylanır.", transactionId));
        return PaymentResult.success(transactionId, getMethodName(), amount);
    }

    @Override
    public String getMethodName() { return "Havale/EFT"; }
}
