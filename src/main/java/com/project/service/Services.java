package com.project.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.domain.cargo.CargoPricingBuilder;
import com.project.domain.cargo.CargoProvider;
import com.project.domain.order.Order;
import com.project.domain.payment.PaymentResult;
import com.project.domain.payment.PaymentStrategy;
import com.project.infrastructure.logger.SystemLogger;

/**
 * Ödeme ve kargo servis sınıfları.
 */
public class Services {

    // ─────────────────────────────────────────────────
    // Ödeme Servisi
    // ─────────────────────────────────────────────────

    /**
     * Ödeme yönetim servisi.
     *
     * <p>Strategy Pattern: Ödeme yöntemi runtime'da enjekte edilir.
     * Bu servis hangi yöntem kullanıldığını bilmez; sadece PaymentStrategy
     * arayüzüyle konuşur.</p>
     */
    @Service
    public static class PaymentService {

        private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
        private final SystemLogger logger = SystemLogger.getInstance();

        /**
         * Ödeme işlemini gerçekleştirir.
         *
         * @param order    Ödenecek sipariş
         * @param strategy Kullanılacak ödeme stratejisi
         * @param details  Ödeme detayları (kart no, iban vb)
         * @return Ödeme sonucu
         */
        public PaymentResult processPayment(Order order, PaymentStrategy strategy, Map<String, String> details) {
            logger.logCriticalOperation("PAYMENT_INIT",
                String.format("Ödeme başlatıldı | Sipariş: %s | Yöntem: %s | Tutar: %.2f TL",
                    order.getId(), strategy.getMethodName(), order.getGrandTotal()));

            PaymentResult result = strategy.pay(order, order.getGrandTotal(), details);

            if (result.isSuccess()) {
                order.setPaid(true);
                logger.logCriticalOperation("PAYMENT_COMPLETE",
                    String.format("Ödeme tamamlandı | Sipariş: %s | TX: %s",
                        order.getId(), result.getTransactionId()));
                log.info("Ödeme başarılı | Sipariş: {} | Yöntem: {} | Tutar: {} TL | TX: {}",
                    order.getId(), result.getPaymentMethod(), result.getAmount(), result.getTransactionId());
            } else {
                logger.error("Ödeme başarısız! Sipariş: " + order.getId() +
                    " | Hata: " + result.getErrorMessage());
                log.warn("Ödeme başarısız | Sipariş: {} | Hata: {}", order.getId(), result.getErrorMessage());
            }

            return result;
        }
    }

    // ─────────────────────────────────────────────────
    // Kargo Servisi
    // ─────────────────────────────────────────────────

    /**
     * Kargo yönetim servisi.
     *
     * <p>Adapter Pattern: Farklı kargo şirketlerinin API'lerini
     * CargoProvider arayüzü üzerinden yönetir. Hangi firma kullanıldığı
     * bu servisin dışında belirlenir (DIP).</p>
     *
     * <p>Decorator Pattern: CargoPricingBuilder ile ek hizmetler
     * (sigorta, kırılgan eşya vb.) fiyata eklenir.</p>
     */
    @Service
    public static class CargoService {

        private static final Logger log = LoggerFactory.getLogger(CargoService.class);
        private final SystemLogger logger = SystemLogger.getInstance();

        /**
         * Siparişin kargo fiyatını hesaplar.
         *
         * @param provider      Kargo sağlayıcısı (Adapter)
         * @param order         Sipariş
         * @param distanceKm    Mesafe (km)
         * @param withInsurance Sigortalı mı?
         * @param withFragile   Kırılgan koruma mı?
         * @return Hesaplanan kargo ücreti
         */
        public double calculateShippingCost(CargoProvider provider, Order order,
                                            double distanceKm, boolean withInsurance,
                                            boolean withFragile) {
            if (distanceKm <= 0) {
                throw new IllegalArgumentException("Mesafe 0 veya negatif olamaz.");
            }
            double weightKg = order.getTotalWeightKg();

            // Decorator zinciri: temel fiyat + ek hizmetler
            CargoPricingBuilder builder = new CargoPricingBuilder(provider);
            if (withInsurance) builder.withInsurance();
            if (withFragile)   builder.withFragileProtection();

            var pricing = builder.build();
            double cost = pricing.calculatePrice(weightKg, distanceKm);

            logger.info(String.format("Kargo fiyatı hesaplandı | %s | %.1f kg | %.0f km | %.2f TL | %s",
                provider.getCompanyName(), weightKg, distanceKm, cost, pricing.getDescription()));

            log.info("Kargo fiyatı hesaplandı | Şirket: {} | kg: {} | km: {} | Ücret: {} | {}",
                provider.getCompanyName(), weightKg, distanceKm, cost, pricing.getDescription());

            return cost;
        }

        /**
         * Takip numarası üretir ve siparişe atar.
         *
         * @param provider     Kargo sağlayıcısı
         * @param order        Sipariş
         * @param senderCity   Gönderici şehri
         * @param receiverCity Alıcı şehri
         * @return Üretilen takip numarası
         */
        public String generateAndAssignTracking(CargoProvider provider, Order order,
                                                String senderCity, String receiverCity) {
            String trackingNo = provider.generateTrackingNumber(order.getId(), senderCity, receiverCity);
            order.setTrackingNumber(trackingNo);

            logger.logCriticalOperation("CARGO_TRACKING",
                String.format("Takip numarası atandı | Sipariş: %s | Kargo: %s | Takip: %s",
                    order.getId(), provider.getCompanyName(), trackingNo));

            log.info("Takip numarası atandı | Sipariş: {} | Kargo: {} | Takip: {} | Tahmini gün: {}",
                order.getId(), provider.getCompanyName(), trackingNo, provider.estimateDeliveryDays(0));

            return trackingNo;
        }
    }
}
