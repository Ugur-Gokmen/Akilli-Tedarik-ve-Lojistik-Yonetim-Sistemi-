package com.project.domain.cargo;

/**
 * Standart kargo sağlayıcı arayüzü - Adapter Pattern'in Target'ı.
 */
public interface CargoProvider {

    String generateTrackingNumber(String orderId, String senderCity, String receiverCity);

    double calculateBasePrice(double weightKg, double distanceKm);

    String getCompanyName();

    int estimateDeliveryDays(double distanceKm);
}
