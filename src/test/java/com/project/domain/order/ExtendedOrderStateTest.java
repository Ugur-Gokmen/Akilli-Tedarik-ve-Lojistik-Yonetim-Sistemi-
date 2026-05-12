package com.project.domain.order;

import com.project.domain.user.Role;
import com.project.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Extended Order State Tests - QA Analysis")
class ExtendedOrderStateTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(new User("qa_test", "qa@mail.com", "hash", Role.CUSTOMER));
        order.setPaid(true); // Default setup for happy paths
    }

    @Test
    @DisplayName("Illegal geçiş fırlatıldığında Siparişin mevcut State'i BOZULMAMALIDIR (Atomic Transition)")
    void givenPendingOrder_whenShipFails_thenStateRemainsPending() {
        OrderState initialState = order.getCurrentState();

        assertThatThrownBy(() -> order.ship())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Beklemedeki sipariş kargoya verilemez");

        // Nesnenin integrity'si korunmuş mu?
        assertThat(order.getCurrentState()).isEqualTo(initialState);
    }

    @ParameterizedTest
    @CsvSource({
        "ONAYLANDI, approve, Sipariş zaten onaylandı!",
        "HAZIRLANIYOR, cancel, Hazırlama aşamasındaki sipariş iptal edilemez",
        "KARGODA, startPreparing, Kargodaki sipariş tekrar hazırlanamaz!"
    })
    @DisplayName("Edge Case: Yanlış state geçiş matrisi")
    void shouldPreventIllegalStateTransitions(String setupState, String action, String expectedError) {
        // Setup internal state recursively
        forceOrderToState(order, setupState);

        assertThatThrownBy(() -> triggerAction(order, action))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(expectedError);
    }

    // Test Helper
    private void triggerAction(Order o, String action) {
        switch(action) {
            case "approve" -> o.approve();
            case "cancel" -> o.cancel();
            case "startPreparing" -> o.startPreparing();
            case "ship" -> o.ship();
            case "deliver" -> o.deliver();
            case "returnOrder" -> o.returnOrder();
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    private void forceOrderToState(Order o, String targetState) {
        switch (targetState) {
            case "ONAYLANDI":
                o.approve();
                break;
            case "HAZIRLANIYOR":
                o.approve();
                o.startPreparing();
                break;
            case "KARGODA":
                o.approve();
                o.startPreparing();
                o.setTrackingNumber("TRK-123");
                o.setShippingCost(10.0);
                o.ship();
                break;
            case "TESLİM EDİLDİ":
                o.approve();
                o.startPreparing();
                o.setTrackingNumber("TRK-123");
                o.setShippingCost(10.0);
                o.ship();
                o.deliver();
                break;
            case "İADE":
                o.approve();
                o.startPreparing();
                o.setTrackingNumber("TRK-123");
                o.setShippingCost(10.0);
                o.ship();
                o.returnOrder();
                break;
        }
    }
}
