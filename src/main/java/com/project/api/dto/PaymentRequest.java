package com.project.api.dto;

import java.util.Map;

/**
 * Ödeme isteği DTO - immutable record.
 *
 * <p>Strategy seçimi Spring bean ismiyle yapılır:
 * "creditCardStrategy", "bankTransferStrategy", "cryptoStrategy"</p>
 *
 * @param orderId         Ödeme yapılacak sipariş ID'si
 * @param strategyBeanName Spring bean adı (ödeme yöntemi)
 * @param details         Ödemeye özel detaylar (kart no, IBAN, cüzdan vb.)
 */
public record PaymentRequest(
    String orderId,
    String strategyBeanName,
    Map<String, String> details
) {}
