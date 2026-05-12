package com.project.domain.cargo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CargoPricingBuilderTest {

    @Test
    void decorators_should_apply_expected_fees() {
        CargoProvider provider = new CargoProvider() {
            @Override public String getCompanyName() { return "TEST"; }
            @Override public double calculateBasePrice(double weightKg, double distanceKm) { return 100.0; }
            @Override public String generateTrackingNumber(String orderId, String senderCity, String receiverCity) { return "T"; }
            @Override public int estimateDeliveryDays(double distanceKm) { return 1; }
        };

        CargoPricingComponent base = new CargoPricingBuilder(provider).build();
        assertEquals(100.0, base.calculatePrice(1, 1), 0.0001);

        CargoPricingComponent insured = new CargoPricingBuilder(provider).withInsurance().build();
        // %1.5 = 1.5 TL ama minimum 10 TL uygulanır => 110
        assertEquals(110.0, insured.calculatePrice(1, 1), 0.0001);

        CargoPricingComponent fragile = new CargoPricingBuilder(provider).withFragileProtection().build();
        assertEquals(135.0, fragile.calculatePrice(1, 1), 0.0001);

        CargoPricingComponent insuredAndFragile = new CargoPricingBuilder(provider)
            .withInsurance()
            .withFragileProtection()
            .build();
        assertEquals(145.0, insuredAndFragile.calculatePrice(1, 1), 0.0001);
    }
}

