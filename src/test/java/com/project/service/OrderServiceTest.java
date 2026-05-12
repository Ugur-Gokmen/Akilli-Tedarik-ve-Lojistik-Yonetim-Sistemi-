package com.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.project.domain.notification.event.StockLowEvent;
import com.project.domain.order.Order;
import com.project.domain.order.OrderItem;
import com.project.domain.order.OrderStates;
import com.project.domain.payment.PaymentResult;
import com.project.domain.payment.PaymentStrategy;
import com.project.domain.product.SimpleProduct;
import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.repository.OrderRepository;
import com.project.repository.ProductRepository;
import com.project.service.Services.PaymentService;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Testleri")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentStrategy paymentStrategy;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, productRepository, eventPublisher, paymentService);
    }

    @Test
    @DisplayName("Ödeme başarılıysa servis sonucu aynen döndürür")
    void givenValidOrderAndStrategy_whenPay_thenDelegatesToPaymentService() {
        User customer = new User("musteri", "m@mail.com", "hash", Role.CUSTOMER);
        Order order = new Order(customer);
        PaymentResult expected = PaymentResult.success("TX-1", "Kredi Kartı", 100.0);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentService.processPayment(eq(order), eq(paymentStrategy), any(Map.class))).thenReturn(expected);

        PaymentResult actual = orderService.pay(order.getId(), paymentStrategy, Map.of("cardNumber", "1111"));

        assertEquals(expected, actual);
        verify(paymentService).processPayment(eq(order), eq(paymentStrategy), any(Map.class));
    }

    @Test
    @DisplayName("Ürünsüz sipariş onaylanamaz")
    void givenOrderWithoutItems_whenApprove_thenThrowsIllegalStateException() {
        User customer = new User("staff", "s@mail.com", "hash", Role.STAFF);
        Order order = new Order(customer);
        order.setPaid(true);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> orderService.approveOrder(order.getId()));
        assertEquals("Ürün içermeyen sipariş onaylanamaz! Sipariş: " + order.getId(), ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Ödenmiş ve ürünlü sipariş onaylandığında state ONAYLANDI olur")
    void givenPaidOrderWithItems_whenApprove_thenStateBecomesApproved() {
        User customer = new User("staff2", "s2@mail.com", "hash", Role.STAFF);
        Order order = new Order(customer);
        SimpleProduct product = new SimpleProduct("Kalem", "SKU-1", 10.0, 0.1, 50, 5);
        order.addItem(new OrderItem(product, 1));
        order.setPaid(true);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.approveOrder(order.getId());

        assertEquals("ONAYLANDI", order.getCurrentStateName());
        assertInstanceOf(OrderStates.ApprovedState.class, order.getCurrentState());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Stok eşiği altına düşerse StockLowEvent publish edilir")
    void givenStockFallsBelowThreshold_whenAddItemToOrder_thenPublishesStockLowEvent() {
        User customer = new User("cust", "c@mail.com", "hash", Role.CUSTOMER);
        Order order = new Order(customer);
        SimpleProduct lowStockProduct = new SimpleProduct("SSD", "SSD-1", 100.0, 0.2, 6, 5);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(productRepository.findById(lowStockProduct.getId())).thenReturn(Optional.of(lowStockProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.addItemToOrder(order.getId(), lowStockProduct.getId(), 1);

        verify(eventPublisher).publishEvent(any(StockLowEvent.class));
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Boş takip numarasıyla kargolama yapılamaz")
    void givenBlankTrackingNumber_whenShipOrder_thenThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> orderService.shipOrder("ORD-1", " ", 20.0));

        assertEquals("Takip numarası boş olamaz.", ex.getMessage());
    }
}
