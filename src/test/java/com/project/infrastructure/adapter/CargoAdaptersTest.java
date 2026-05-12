package com.project.infrastructure.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cargo Adapters Tests - QA Analysis")
class CargoAdaptersTest {

    @Test
    @DisplayName("GlobalExpress zone tabanlı fiyatlandırması boundary değerlerde doğru çalışmalı")
    void globalExpressShouldCalculateZonePricesCorrectly() {
        // Arrange
        CargoAdapters.GlobalExpressAdapter adapter = new CargoAdapters.GlobalExpressAdapter(true);

        // Act: 199 km = Zone 1, 201 km = Zone 2
        double priceZone1 = adapter.calculateBasePrice(10.0, 199.0);
        double priceZone2 = adapter.calculateBasePrice(10.0, 201.0);

        // Assert
        assertThat(priceZone2).isGreaterThan(priceZone1);
    }

    @Test
    @DisplayName("Ağırlık 0 veya negatif olduğunda sistem nasıl davranıyor?")
    void negativeWeightShouldBeHandled() {
        CargoAdapters.YurticiCargoAdapter adapter = new CargoAdapters.YurticiCargoAdapter("IST");

        // Eğer sistem negatif ağırlığı handle edemiyorsa bu test bir BUG yakalar!
        // QA report assumed it should throw IllegalArgumentException. Let's see if the code actually handles it.
        // It might fail, which is intended.
        assertThatThrownBy(() -> adapter.calculateBasePrice(-5.0, 100.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("negatif olamaz");
    }
}
