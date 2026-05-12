package com.project.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.api.dto.PaymentRequest;
import com.project.api.dto.ShippingRequest;
import com.project.domain.order.Order;
import com.project.domain.payment.PaymentResult;
import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.infrastructure.security.RequireRole;
import com.project.service.OrderService;
import com.project.service.PaymentApplicationService;
import com.project.service.ShippingApplicationService;

/**
 * Sipariş işlemlerini yöneten ana REST Kontrolcüsü.
 * Bu sınıf, iş mantığını koordine etmek için OrderService ve CargoService kullanır.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;
    private final PaymentApplicationService paymentApplicationService;
    private final ShippingApplicationService shippingApplicationService;

    /**
     * Constructor Injection: Spring'in bağımlılıkları güvenli bir şekilde bağlamasını sağlar.
     */
    @Autowired
    public OrderRestController(OrderService orderService,
                               PaymentApplicationService paymentApplicationService,
                               ShippingApplicationService shippingApplicationService) {
        this.orderService = orderService;
        this.paymentApplicationService = paymentApplicationService;
        this.shippingApplicationService = shippingApplicationService;
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
    public PaymentResult processPayment(@PathVariable String id, @RequestBody PaymentRequest request) {
        if (request.orderId() != null && !id.equals(request.orderId())) {
            throw new IllegalArgumentException("Path parametresi ile body içindeki orderId aynı olmalıdır.");
        }
        Map<String, String> details = request.details() == null ? Map.of() : request.details();
        return paymentApplicationService.processPayment(id, request.strategyBeanName(), details);
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
        try {
            var result = shippingApplicationService.shipOrder(
                id,
                request.company(),
                request.senderCity(),
                request.receiverCity(),
                request.distanceKm(),
                request.withInsurance(),
                request.withFragile());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Sipariş kargolandı",
                "trackingNumber", result.trackingNumber(),
                "shippingCost", result.shippingCost()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Kargo işlemi başarısız: " + e.getMessage());
        }
    }
}