package com.project.service;

import java.util.Optional;

import com.project.domain.order.Order;
import com.project.domain.order.OrderItem;
import com.project.domain.product.Product;
import com.project.domain.user.AuthorizationGuard;
import com.project.domain.user.User;
import com.project.infrastructure.logger.SystemLogger;
import com.project.repository.OrderRepository;
import com.project.repository.ProductRepository;
import java.util.List;
import com.project.infrastructure.security.RequireRole;
import com.project.domain.user.Role;
import com.project.domain.notification.event.StockLowEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sipariş yönetim servisi.
 *
 * <p>Sipariş oluşturma, durum geçişleri ve sorgulama işlemlerini yönetir.
 * State Pattern sayesinde durum geçişlerinde hiçbir if-else bulunmaz;
 * tüm mantık Order nesnesi üzerinden State'lere delege edilir.</p>
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SystemLogger logger = SystemLogger.getInstance();

    /**
     * @param orderRepository   Sipariş deposu
     * @param productRepository Ürün deposu
     * @param eventPublisher    Event publisher
     */
    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Yeni sipariş oluşturur.
     *
     * @param customer Siparişi veren müşteri
     * @return Oluşturulan sipariş
     */
    @Transactional
    public Order createOrder(User customer) {
        // [SENIOR DEFENSIVE CHECK]: Verilen parametre geçerli mi?
        if (customer == null) {
            throw new IllegalArgumentException("Sipariş oluşturmak için müşteri bilgisi gerekli (null olamaz).");
        }
        Order order = new Order(customer);
        orderRepository.save(order);
        logger.info("Sipariş oluşturuldu: " + order.getId() + " | Müşteri: " + customer.getUsername());
        return order;
    }

    /**
     * Siparişe ürün ekler ve stoktan düşer.
     *
     * @param orderId   Sipariş ID'si
     * @param productId Ürün ID'si
     * @param quantity  Miktar
     */
    @Transactional
    public void addItemToOrder(String orderId, String productId, int quantity) {
        // [SENIOR DEFENSIVE CHECK]: Miktar negatif veya sıfır olamaz. Eksi stok düşüşü/artışı güvenlik açığıdır.
        if (quantity <= 0) {
            throw new IllegalArgumentException(String.format("Ürün miktarı pozitif olmalıdır! İstenen: %d", quantity));
        }
        if (orderId == null || productId == null) {
            throw new IllegalArgumentException("Sipariş ID veya Ürün ID null olamaz.");
        }

        Order order = findOrderOrThrow(orderId);
        Product product = findProductOrThrow(productId);
        OrderItem item = new OrderItem(product, quantity);
        order.addItem(item);
        
        // [EVENT-DRIVEN]: Eğer ürün stoku kritik eşiğe düştüyse Spring Event fırlat
        // SUNUM NOTU: Burada doğrudan "notifier.sendEmail(...)" yazmıyoruz (Decoupled).
        // Sadece "Stok azaldı" diye bir anons yapıyoruz (Publish).
        // Sistemi dinleyen (Subscribe) Observer servisler bu mesajı alıp kendi görevlerini yapar.
        if (product.getStock() <= product.getStockThreshold()) {
            eventPublisher.publishEvent(new StockLowEvent(this, product.getId(), product.getName(), product.getStock(), product.getStockThreshold()));
        }

        orderRepository.save(order);
    }

    /**
     * Siparişi onaylar (STAFF/ADMIN).
     *
     * @param orderId Sipariş ID'si
     */
    @RequireRole(Role.STAFF) // [AOP SECURITY]
    @Transactional
    public void approveOrder(String orderId) {
        Order order = findOrderOrThrow(orderId);
        order.approve();
        orderRepository.save(order);
        System.out.printf("✅ Sipariş onaylandı: %s%n", orderId);
    }

    /**
     * Sipariş hazırlamayı başlatır (STAFF/ADMIN).
     *
     * @param orderId Sipariş ID'si
     */
    @RequireRole(Role.STAFF) // [AOP SECURITY]
    @Transactional
    public void startPreparing(String orderId) {
        Order order = findOrderOrThrow(orderId);
        order.startPreparing();
        orderRepository.save(order);
        System.out.printf("📦 Sipariş hazırlanıyor: %s%n", orderId);
    }

    /**
     * Siparişi kargoya verir - takip numarası atar.
     *
     * @param orderId        Sipariş ID'si
     * @param trackingNumber Kargo takip numarası
     * @param shippingCost   Kargo ücreti
     */
    @RequireRole(Role.STAFF) // [AOP SECURITY]
    @Transactional
    public void shipOrder(String orderId,
                          String trackingNumber, double shippingCost) {
        // [SENIOR DEFENSIVE CHECK]: Teslimat bedeli negatif girilerek toplam fatura tutarının düşürülmesi engelleniyor.
        if (shippingCost < 0) {
            throw new IllegalArgumentException(String.format("Kargo ücreti negatif olamaz! Girilen: %.2f TL", shippingCost));
        }
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Takip numarası boş olamaz.");
        }

        Order order = findOrderOrThrow(orderId);
        order.setTrackingNumber(trackingNumber);
        order.setShippingCost(shippingCost);
        order.ship();
        orderRepository.save(order);
        System.out.printf("🚚 Sipariş kargoya verildi: %s | Takip: %s | Kargo: %.2f TL%n",
            orderId, trackingNumber, shippingCost);
    }

    /**
     * Siparişi teslim edildi olarak işaretler (STAFF/ADMIN).
     *
     * @param orderId Sipariş ID'si
     */
    @RequireRole(Role.STAFF) // [AOP SECURITY]
    @Transactional
    public void deliverOrder(String orderId) {
        Order order = findOrderOrThrow(orderId);
        order.deliver();
        orderRepository.save(order);
        System.out.printf("🎉 Sipariş teslim edildi: %s%n", orderId);
    }

    /**
     * İade başlatır (Kargoda → İade).
     *
     * @param orderId Sipariş ID'si
     */
    @RequireRole(Role.CUSTOMER) // Müşteri de iade yapabilsin
    @Transactional
    public void returnOrder(String orderId) {
        Order order = findOrderOrThrow(orderId);
        // Müşteri kendi siparişini iade edebilir
        order.returnOrder();
        orderRepository.save(order);
        System.out.printf("↩️  İade başlatıldı: %s%n", orderId);
    }

    /**
     * Siparişi iptal eder.
     *
     * @param orderId Sipariş ID'si
     */
    @RequireRole({Role.CUSTOMER, Role.STAFF})
    @Transactional
    public void cancelOrder(String orderId) {
        Order order = findOrderOrThrow(orderId);
        order.cancel();
        orderRepository.save(order);
        System.out.printf("❌ Sipariş iptal edildi: %s%n", orderId);
    }

    /**
     * Tüm siparişleri listeler (ADMIN/STAFF).
     *
     * @return Sipariş listesi
     */
    @RequireRole(Role.STAFF)
    public List<Order> listAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Müşterinin kendi siparişlerini listeler.
     *
     * @param customer Müşteri
     * @return Sipariş listesi
     */
    public List<Order> listCustomerOrders(User customer) {
        return orderRepository.findByCustomerId(customer.getId());
    }

    /**
     * Sipariş ID ile arar.
     *
     * @param orderId Sipariş ID'si
     * @return Sipariş (varsa)
     */
    public Optional<Order> findById(String orderId) {
        return orderRepository.findById(orderId);
    }

    // --- Yardımcı Metodlar ---

    private Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı: " + orderId));
    }

    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı: " + productId));
    }
}
