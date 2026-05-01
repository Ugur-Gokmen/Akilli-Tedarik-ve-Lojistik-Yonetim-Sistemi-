package com.project.domain.payment;


import com.project.domain.order.Order;
import com.project.infrastructure.logger.SystemLogger;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Kripto para ödeme stratejisi - Strategy Pattern concrete implementasyonu.
 *
 * <p>Ödev şartnamesinde "ileride eklenebilecek" olarak belirtilen yöntem.
 * Strategy Pattern sayesinde mevcut kodda HİÇBİR DEĞİŞİKLİK gerekmedi.</p>
 */
@Component("cryptoStrategy")
public class CryptoPayment implements PaymentStrategy {

    private static final SystemLogger logger = SystemLogger.getInstance();

    @Override
    public PaymentResult pay(Order order, double amount, Map<String, String> details) {
        String walletAddress = details.getOrDefault("walletAddress", "UNKNOWN_WALLET");
        String currency = details.getOrDefault("currency", "BTC");
        double cryptoAmount = amount / 3_500_000.0; // Simülasyon: 1 BTC = 3.5M TL
        logger.logCriticalOperation("PAYMENT",
            String.format("Kripto ödeme başlatıldı | Sipariş: %s | %.2f TL = %.8f %s | Cüzdan: %s",
                order.getId(), amount, cryptoAmount, currency, walletAddress));
        String transactionId = "CRYPTO-" + System.currentTimeMillis();
        logger.logCriticalOperation("PAYMENT_SUCCESS",
            String.format("Kripto işlem yayınlandı | Tx Hash: %s | Onay bekleniyor...", transactionId));
        return PaymentResult.success(transactionId, getMethodName(), amount);
    }

    @Override
    public String getMethodName() { return "Kripto Para"; }
}
