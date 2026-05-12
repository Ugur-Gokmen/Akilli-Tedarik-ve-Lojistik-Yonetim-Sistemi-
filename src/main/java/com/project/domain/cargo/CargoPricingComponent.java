package com.project.domain.cargo;

/**
 * Kargo fiyatlandırma bileşeni arayüzü - Decorator Pattern'in Component'i.
 *
 * <p>Hem temel fiyat (BaseCargoPrice) hem de tüm dekoratörler
 * (Sigorta, Kırılgan, Express vb.) bu arayüzü implement eder.</p>
 */
public interface CargoPricingComponent {

    /**
     * Toplam kargo ücretini hesaplar.
     *
     * @param weightKg   Paket ağırlığı (kg)
     * @param distanceKm Mesafe (km)
     * @return Hesaplanan ücret (TL)
     */
    double calculatePrice(double weightKg, double distanceKm);

    /**
     * Fiyatlandırma açıklamasını döner.
     *
     * @return Hangi ek hizmetlerin dahil olduğunu gösteren açıklama
     */
    String getDescription();
}
