package com.project.service;

import com.project.domain.notification.event.StockLowEvent;
import com.project.domain.order.Order;
import com.project.domain.order.OrderItem;
import com.project.domain.payment.PaymentResult;
import com.project.domain.payment.PaymentStrategy;
import com.project.domain.product.Product;
import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.infrastructure.logger.SystemLogger;
import com.project.infrastructure.security.RequireRole;
import com.project.repository.OrderRepository;
import com.project.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sipariş yönetim servisi.
 *
 * <p>Sipariş oluşturma, durum geçişleri ve ödeme işlemlerini koordine eder.
 * State Pattern sayesinde durum geçişlerinde hiçbir if-else bulunmaz.</p>
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentService paymentService;
    private final SystemLogger logger = SystemLogger.getInstance();

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        ApplicationEventPublisher eventPublisher,
                        PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.paymentService = paymentService;
    }

    // ─────────────────────────────────────────────────
    // Sipariş Oluşturma
    // ─────────────────────────────────────────────────

    @Transactional
    public Order createOrder(User customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Sipariş için müşteri bilgisi gerekli.");
        }
        Order order = new Order(customer);
        orderRepository.save(order);
        logger.info("Sipariş oluşturuldu: " + order.getId() + " | Müşteri: " + customer.getUsername());
        return order;
    }

    // ─────────────────────────────────────────────────
    // Ürün Ekleme
    // ─────────────────────────────────────────────────

    @Transactional
    public void addItemToOrder(String orderId, String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Ürün miktarı pozitif olmalı! Girilen: " + quantity);
        }
        Order order = findOrderOrThrow(orderId);
        Product product = findProductOrThrow(productId);

        OrderItem item = new OrderItem(product, quantity);
        order.addItem(item);

        // Event-Driven Observer: stok kritik eşiğe düştüyse yayınla
        if (product.getStock() <= product.getStockThreshold()) {
            eventPublisher.publishEvent(new StockLowEvent(
                this, product.getId(), product.getName(),
                product.getStock(), product.getStockThreshold()));
        }

        orderRepository.save(order);
    }

    // ─────────────────────────────────────────────────
    // Ödeme
    // ─────────────────────────────────────────────────

    /**
     * Sipariş ödemesini işler. REST controller'lar tarafından kullanılır.
     *
     * @param orderId  Sipariş ID'si
     * @param strategy Ödeme stratejisi
     * @param details  Ödeme detayları (kart no, iban vb.)
     * @return Ödeme sonucu
     */
    @Transactional
    public PaymentResult pay(String orderId, PaymentStrategy strategy, Map<String, String> details) {
        Order order = findOrderOrThrow(orderId);
        return paymentService.processPayment(order, strategy, details);
    }

    // ─────────────────────────────────────────────────
    // Durum Geçişleri — State Pattern
    // ─────────────────────────────────────────────────

    @RequireRole(Role.STAFF)
    @Transactional
    public void approveOrder(String orderId) {
        Order order = findOrderOrThrow(orderId);
        order.approve();
        orderRepository.save(order);
        System.out.printf("✅ Sipariş onaylandı: %s%n", orderId);
    }

    @RequireRole(Role.STAFF)
    @Transactional
    public void startPreparing(String orderId) {
        Order order = findOrderOrThrow(orderId);
        order.startPreparing();
        orderRepository.save(order);
        System.out.printf("📦 Sipariş hazırlanıyor: %s%n", orderId);
    }

    @RequireRole(Role.STAFF)
    @Transactional
    public void shipOrder(String orderId, String trackingNumber, double shippingCost) {
        if (shippingCost < 0) {
            throw new IllegalArgumentException("Kargo ücreti negatif olamaz!");
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("Takip numarası boş olamaz.");
        }
        Order order = findOrderOrThrow(orderId);
        order.setTrackingNumber(trackingNumber);
        order.setShippingCost(shippingCost);
        order.ship();
        orderRepository.save(order);
        System.out.printf("🚚 Kargoya verildi: %s | Takip: %s | Ücret: %.2f TL%n",
            orderId, trackingNumber, shippingCost);
    }

    @RequireRole(Role.STAFF)
    @Transactional
    public void deliverOrder(String orderId) {
        Order order = findOrderOrThrow(orderId);
        order.deliver();
        orderRepository.save(order);
        System.out.printf("🎉 Teslim edildi: %s%n", orderId);
    }

    @RequireRole(Role.CUSTOMER)
    @Transactional
    public void returnOrder(String orderId) {
        Order order = findOrderOrThrow(orderId);
        order.returnOrder();
        orderRepository.save(order);
        System.out.printf("↩️  İade başlatıldı: %s%n", orderId);
    }

    @RequireRole({Role.CUSTOMER, Role.STAFF})
    @Transactional
    public void cancelOrder(String orderId) {
        Order order = findOrderOrThrow(orderId);
        order.cancel();
        orderRepository.save(order);
        System.out.printf("❌ İptal edildi: %s%n", orderId);
    }

    // ─────────────────────────────────────────────────
    // Sorgulama
    // ─────────────────────────────────────────────────

    @RequireRole(Role.STAFF)
    public List<Order> listAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> listCustomerOrders(User customer) {
        return orderRepository.findByCustomerId(customer.getId());
    }

    public Optional<Order> findById(String orderId) {
        return orderRepository.findById(orderId);
    }

    // ─────────────────────────────────────────────────
    // Yardımcı
    // ─────────────────────────────────────────────────

    private Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı: " + orderId));
    }

    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı: " + productId));
    }
}