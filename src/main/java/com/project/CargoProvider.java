package com.project.domain.cargo;

/**
 * Standart kargo sağlayıcı arayüzü - Adapter Pattern'in Target'ı.
 *
 * <p>Adapter Pattern: Aras, Yurtiçi, GlobalExpress gibi her kargo firmasının
 * kendi API'si farklıdır. Bu arayüz, tüm firmaları ortak bir sözleşme altında
 * buluşturur. Sistem her zaman bu arayüzle konuşur — alt yapıdaki gerçek API
 * hiç görülmez.</p>
 *
 * <p>Yeni bir kargo firması eklemek: sadece bu arayüzü implemente eden bir
 * Adapter sınıfı yazmak yeterlidir → Open/Closed Principle.</p>
 */
public interface CargoProvider {

    /**
     * Gönderi takip numarası üretir.
     *
     * @param orderId   Sipariş kimliği
     * @param senderCity Gönderici şehri
     * @param receiverCity Alıcı şehri
     * @return Takip numarası
     */
    String generateTrackingNumber(String orderId, String senderCity, String receiverCity);

    /**
     * Kargo ücretini hesaplar (temel ücret - dekoratörlerden önce).
     *
     * @param weightKg   Paket ağırlığı (kg)
     * @param distanceKm Mesafe (km)
     * @return Temel kargo ücreti (TL)
     */
    double calculateBasePrice(double weightKg, double distanceKm);

    /**
     * Kargo şirketinin adını döner.
     *
     * @return Şirket adı
     */
    String getCompanyName();

    /**
     * Tahmini teslimat süresini döner.
     *
     * @param distanceKm Mesafe (km)
     * @return Tahmini gün sayısı
     */
    int estimateDeliveryDays(double distanceKm);
}
