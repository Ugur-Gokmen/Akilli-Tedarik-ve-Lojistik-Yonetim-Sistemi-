package com.project.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.api.dto.ShippingRequest;
import com.project.domain.cargo.CargoProvider;
import com.project.domain.order.Order;
import com.project.domain.payment.PaymentResult;
import com.project.domain.payment.PaymentStrategy;
import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.infrastructure.factory.CargoProviderFactory;
import com.project.infrastructure.security.RequireRole;
import com.project.service.OrderService;
import com.project.service.Services.CargoService; // Paket yolunu projenize göre kontrol edin

/**
 * Sipariş işlemlerini yöneten ana REST Kontrolcüsü.
 * Bu sınıf, iş mantığını koordine etmek için OrderService ve CargoService kullanır.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;
    private final CargoService cargoService; // Eksik olan bağımlılık eklendi

    /**
     * Constructor Injection: Spring'in bağımlılıkları güvenli bir şekilde bağlamasını sağlar.
     */
    @Autowired
    public OrderRestController(OrderService orderService, CargoService cargoService) {
        this.orderService = orderService;
        this.cargoService = cargoService;
    }

    // --- Sipariş Oluşturma ve Ürün Yönetimi ---

    @PostMapping("/create")
    public Order createOrder(@RequestBody User customer) {
        return orderService.createOrder(customer);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<String> addItem(@PathVariable String id, @RequestParam String productId, @RequestParam int qty) {
        orderService.addItemToOrder(id, productId, qty);
        return ResponseEntity.ok("Ürün başarıyla eklendi.");
    }

    // --- Ödeme İşlemleri ---

    @PostMapping("/{id}/payment")
    public PaymentResult processPayment(@PathVariable String id, @RequestBody PaymentStrategy strategy, @RequestBody Map<String, String> details) {
        // PathVariable olarak nesne yerine ID kullanmak API standartlarına daha uygundur
        return orderService.pay(id, strategy, details);
    }

    // --- Personel Yetkisi Gerektiren İşlemler ---

    @GetMapping
    @RequireRole(Role.STAFF)
    public List<Order> listAllOrders() {
        return orderService.listAllOrders();
    }

    @PutMapping("/{id}/approve")
    @RequireRole(Role.STAFF)
    public ResponseEntity<String> approve(@PathVariable String id) {
        orderService.approveOrder(id);
        return ResponseEntity.ok("Sipariş onaylandı.");
    }

    @PostMapping("/{id}/prepare")
    @RequireRole(Role.STAFF)
    public ResponseEntity<?> prepare(@PathVariable String id) {
        orderService.startPreparing(id); 
        return ResponseEntity.ok("Hazırlık süreci başlatıldı.");
    }

    @PutMapping("/{id}/deliver")
    @RequireRole(Role.STAFF)
    public ResponseEntity<String> deliver(@PathVariable String id) {
        orderService.deliverOrder(id);
        return ResponseEntity.ok("Sipariş teslim edildi.");
    }

    // --- Kargo İşlemleri (Complex Logic) ---

    @PostMapping("/{id}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable String id, @RequestBody ShippingRequest request) {
        // 1. Siparişi veritabanından çek
        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı! ID: " + id));

        // 2. Değişkenleri metodun başında tanımla (Scope güvenliği)
        String tracking = "";
        double cost = 0.0;

        try {
            // Factory Pattern: Enum üzerinden somut sağlayıcıyı al
            CargoProvider provider = CargoProviderFactory.getProvider(request.company());
            
            // Adapter Pattern Entegrasyonu: Kargo hesaplamalarını gerçekleştir
            tracking = cargoService.generateAndAssignTracking(provider, order, request.senderCity(), request.receiverCity());
            cost = cargoService.calculateShippingCost(provider, order, request.distanceKm(), request.withInsurance(), request.withFragile());
            
            // State Pattern Tetikleme: Siparişi 'Shipped' durumuna geçir
            orderService.shipOrder(id, tracking, cost);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Sipariş kargolandı",
                "trackingNumber", tracking,
                "shippingCost", cost
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Kargo işlemi başarısız: " + e.getMessage());
        }
    }
}