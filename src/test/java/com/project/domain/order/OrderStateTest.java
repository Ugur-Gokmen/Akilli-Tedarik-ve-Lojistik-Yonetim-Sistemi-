package com.project.domain.order;

import com.project.domain.user.Role;
import com.project.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sipariş Durum Geçişleri Test Paketi (State Pattern)")
class OrderStateTest {

    private Order order;

    @BeforeEach
    void setUp() {
        // JPA no-arg constructor protected olduğundan, test için gerçek User kullanılır.
        // Entegrasyon testlerinde @DataJpaTest + repository kullanılmalı.
        User testCustomer = new User("test_musteri", "test@mail.com", "hash", Role.CUSTOMER);
        order = new Order(testCustomer);
        // Order zaten PendingState ile başlar — ek setState gerekmez.
    }

    @Test
    @DisplayName("Beklemedeki sipariş onaylandığında durumu ONAYLANDI olmalıdır (Happy Path)")
    void givenPendingOrder_whenApprove_thenStateIsApproved() {
        // Act
        order.approve();

        // Assert
        assertInstanceOf(OrderStates.ApprovedState.class, order.getCurrentState(),
            "Sipariş durumu ApprovedState olmalıdır");
        assertEquals("ONAYLANDI", order.getCurrentStateName());
    }

    @Test
    @DisplayName("Beklemedeki sipariş doğrudan kargoya verilemez (Negative Scenario)")
    void givenPendingOrder_whenShip_thenThrowsIllegalStateException() {
        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> order.ship());
        assertEquals("Beklemedeki sipariş kargoya verilemez!", ex.getMessage());
    }

    @Test
    @DisplayName("İptal edilmiş siparişte hiçbir işlem yapılamaz (Terminal State)")
    void givenCancelledOrder_whenAnyAction_thenThrowsIllegalStateException() {
        // Arrange — beklemeden iptal et
        order.cancel();

        // Assert — tüm işlemler exception fırlatmalı
        assertThrows(IllegalStateException.class, () -> order.approve());
        assertThrows(IllegalStateException.class, () -> order.ship());
        assertThrows(IllegalStateException.class, () -> order.deliver());
    }

    @Test
    @DisplayName("Tam mutlu yol: Pending → Approved → Preparing → Shipped → Delivered")
    void givenPendingOrder_whenFullHappyPath_thenAllStatesTransitionCorrectly() {
        // Arrange: kargo bilgilerini simüle et
        order.setTrackingNumber("TEST-TRACK-001");
        order.setShippingCost(50.0);

        // Act & Assert — her adım doğrulanır
        order.approve();
        assertEquals("ONAYLANDI", order.getCurrentStateName());

        order.startPreparing();
        assertEquals("HAZIRLANIYOR", order.getCurrentStateName());

        order.ship();
        assertEquals("KARGODA", order.getCurrentStateName());

        order.deliver();
        assertEquals("TESLİM EDİLDİ", order.getCurrentStateName());
    }

    @Test
    @DisplayName("Kargodaki sipariş iade edilebilir (Kargoda → İade)")
    void givenShippedOrder_whenReturn_thenStateIsReturned() {
        // Arrange — kargoya kadar ilerlet
        order.approve();
        order.startPreparing();
        order.setTrackingNumber("TRACK-RETURN");
        order.ship();

        // Act
        order.returnOrder();

        // Assert
        assertEquals("İADE", order.getCurrentStateName());
    }

    @Test
    @DisplayName("Kargodaki sipariş iptal edilemez — iade yolu kullanılmalı")
    void givenShippedOrder_whenCancel_thenThrowsIllegalStateException() {
        order.approve();
        order.startPreparing();
        order.ship();

        assertThrows(IllegalStateException.class, () -> order.cancel(),
            "Kargodaki sipariş cancel() ile iptal edilemez");
    }
}
