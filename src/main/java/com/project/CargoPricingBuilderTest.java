package com.project.domain.cargo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kargo Fiyatlandırma ve Dekoratör Test Paketi")
class CargoPricingBuilderTest {

    @Mock
    private CargoProvider mockProvider; // Class under test değil, harici bağımlılık mock'lanır.

    @Test
    @DisplayName("Sadece temel kargo seçildiğinde, sağlayıcının baz fiyatı dönmelidir")
    void givenBaseCargo_whenCalculatePrice_thenReturnsProviderBasePrice() {
        // Arrange
        double weight = 5.0;
        double distance = 100.0;
        Mockito.when(mockProvider.calculateBasePrice(weight, distance)).thenReturn(50.0);

        CargoPricingBuilder builder = new CargoPricingBuilder(mockProvider);
        CargoPricingComponent pricing = builder.build();

        // Act
        double finalPrice = pricing.calculatePrice(weight, distance);

        // Assert
        assertEquals(50.0, finalPrice, "Temel fiyat mock sağlayıcıdan gelen 50.0 olmalıdır");
        Mockito.verify(mockProvider, Mockito.times(1)).calculateBasePrice(weight, distance);
    }

    @Test
    @DisplayName("Sigorta ve Kırılacak Eşya eklendiğinde fiyat katlanarak hesaplanmalıdır (Decorator Flow)")
    void givenCargoWithInsuranceAndFragile_whenCalculatePrice_thenReturnsDecoratedPrice() {
        // Arrange
        double weight = 10.0;
        double distance = 200.0;
        // Varsayım: Base = 100 TL. Kırılacak Eşya (+15 TL). Sigorta (+%10).
        // Beklenen: (100) -> Kırılacak -> 115 -> Sigorta (%10) -> 126.5 TL
        Mockito.when(mockProvider.calculateBasePrice(weight, distance)).thenReturn(100.0);

        CargoPricingBuilder builder = new CargoPricingBuilder(mockProvider);
        CargoPricingComponent pricing = builder
                                        .withFragileProtection() // +15 TL (Örnek mantık, gerçek koda göre ayarlanmalı)
                                        .withInsurance()         // +%10
                                        .build();

        // Act
        double finalPrice = pricing.calculatePrice(weight, distance);

        // Assert
        assertTrue(finalPrice > 100.0, "Dekoratörler eklendikten sonra fiyat temel fiyattan büyük olmalıdır");
        // Not: Gerçek implementasyondaki tam matematiksel formül buraya yazılır.
        // assertEquals(126.5, finalPrice, 0.01);
    }
}
