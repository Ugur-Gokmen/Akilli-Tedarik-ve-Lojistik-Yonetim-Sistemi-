package com.project.domain.cargo;

import com.project.infrastructure.adapter.ThirdPartyCargoAPIs;

public class YurticiCargoProvider implements CargoProvider {
    private final ThirdPartyCargoAPIs.YurticiCargoApi yurticiApi = new ThirdPartyCargoAPIs.YurticiCargoApi();

    @Override
    public String generateTrackingNumber(String orderId, String senderCity, String receiverCity) {
        return yurticiApi.generateBarcode(orderId, receiverCity);
    }

    @Override
    public double calculateBasePrice(double weightKg, double distanceKm) {
        double desi = weightKg * 1.2; // Desi simülasyonu
        return yurticiApi.computePrice(desi, "GENEL", "GENEL");
    }

    @Override
    public String getCompanyName() {
        return "Yurtiçi Kargo";
    }

    @Override
    public int estimateDeliveryDays(double distanceKm) {
        // API String döndüğü için basit bir map yapıyoruz
        String delivery = yurticiApi.getExpectedDelivery(distanceKm < 200);
        return delivery.contains("1-2") ? 2 : 3;
    }
}