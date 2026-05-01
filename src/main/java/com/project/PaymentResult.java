package com.project.domain.payment;

import java.time.LocalDateTime;

/**
 * Ödeme işlemi sonucu - immutable value object.
 *
 * <p>Başarılı veya başarısız ödeme sonucunu taşır.
 * Factory metodları ile oluşturulur (static factory method).</p>
 */
public class PaymentResult {

    private final boolean success;
    private final String transactionId;
    private final String paymentMethod;
    private final double amount;
    private final String errorMessage;
    private final LocalDateTime processedAt;

    private PaymentResult(boolean success, String transactionId,
                          String paymentMethod, double amount, String errorMessage) {
        this.success = success;
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Başarılı ödeme sonucu oluşturur.
     *
     * @param transactionId İşlem kimliği
     * @param method        Ödeme yöntemi
     * @param amount        Ödenen tutar
     * @return Başarılı sonuç
     */
    public static PaymentResult success(String transactionId, String method, double amount) {
        return new PaymentResult(true, transactionId, method, amount, null);
    }

    /**
     * Başarısız ödeme sonucu oluşturur.
     *
     * @param method       Ödeme yöntemi
     * @param errorMessage Hata mesajı
     * @return Başarısız sonuç
     */
    public static PaymentResult failure(String method, String errorMessage) {
        return new PaymentResult(false, null, method, 0, errorMessage);
    }

    public boolean isSuccess() { return success; }
    public String getTransactionId() { return transactionId; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getAmount() { return amount; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getProcessedAt() { return processedAt; }

    @Override
    public String toString() {
        if (success) {
            return String.format("PaymentResult{SUCCESS | method='%s' | amount=%.2f TL | tx='%s'}",
                paymentMethod, amount, transactionId);
        }
        return String.format("PaymentResult{FAILED | method='%s' | error='%s'}",
            paymentMethod, errorMessage);
    }
}
