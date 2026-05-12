package com.project.domain.cargo;

import com.project.infrastructure.adapter.ThirdPartyCargoAPIs;

public class ArasCargoProvider implements CargoProvider {
    private final ThirdPartyCargoAPIs.ArasCargoApi arasApi = new ThirdPartyCargoAPIs.ArasCargoApi();

    @Override
    public String generateTrackingNumber(String orderId, String senderCity, String receiverCity) {
        return arasApi.createShipment(senderCity, receiverCity, orderId);
    }

    @Override
    public double calculateBasePrice(double weightKg, double distanceKm) {
        // API float ve int beklediği için cast yapıyoruz
        return arasApi.getShipmentCost((float) weightKg, (int) distanceKm);
    }

    @Override
    public String getCompanyName() {
        return "Aras Kargo";
    }

    @Override
    public int estimateDeliveryDays(double distanceKm) {
        return arasApi.calculateDeliveryTime((int) distanceKm);
    }
}