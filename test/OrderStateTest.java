package com.project.domain.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sipariş Durum Geçişleri Test Paketi (State Pattern)")
class OrderStateTest {

    private Order order;

    @BeforeEach
    void setUp() {
        // Arrange (Hazırlık) - Her testten önce beklemede olan taze bir sipariş oluşturulur
        order = new Order(); 
        order.setState(new OrderStates.PendingState());
    }

    @Test
    @DisplayName("Beklemedeki sipariş onaylandığında durumu 'ONAYLANDI' olmalıdır (Happy Path)")
    void givenPendingOrder_whenApprove_thenStateIsApproved() {
        // Act (Eylem)
        order.getCurrentState().approve(order);

        // Assert (Doğrulama)
        assertTrue(order.getCurrentState() instanceof OrderStates.ApprovedState, 
            "Sipariş durumu ApprovedState olmalıdır");
        assertEquals("ONAYLANDI", order.getCurrentStateName());
    }

    @Test
    @DisplayName("Beklemedeki sipariş doğrudan kargoya verilmek istendiğinde hata fırlatmalıdır (Negative Scenario)")
    void givenPendingOrder_whenShip_thenThrowsIllegalStateException() {
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            order.getCurrentState().ship(order);
        });

        // Hata mesajının doğru olduğunu doğrula
        assertEquals("Beklemedeki sipariş kargoya verilemez!", exception.getMessage());
    }

    @Test
    @DisplayName("İptal edilmiş bir sipariş üzerinde işlem yapılamamalıdır (Terminal State Rule)")
    void givenCancelledOrder_whenAnyAction_thenThrowsIllegalStateException() {
        // Arrange
        order.setState(new OrderStates.CancelledState());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> order.getCurrentState().approve(order));
        assertThrows(IllegalStateException.class, () -> order.getCurrentState().ship(order));
        assertThrows(IllegalStateException.class, () -> order.getCurrentState().deliver(order));
    }
}
