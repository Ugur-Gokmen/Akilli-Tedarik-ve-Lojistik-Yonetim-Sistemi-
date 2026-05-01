package com.project.api;

import com.project.domain.order.Order;
import com.project.domain.user.User;
import com.project.service.OrderService;
import com.project.infrastructure.security.RequireRole;
import com.project.domain.user.Role;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;

    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Yeni bir sipariş oluşturur.
     */
    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody User customer) {
        Order order = orderService.createOrder(customer);
        return ResponseEntity.ok(order);
    }

    /**
     * Siparişi onaylar. 
     * AOP (SecurityAspect) burada otomatik devreye girer.
     */
    @PutMapping("/{id}/approve")
    @RequireRole(Role.STAFF)
    public ResponseEntity<String> approveOrder(@PathVariable String id) {
        orderService.approveOrder(id);
        return ResponseEntity.ok("Sipariş onaylandı: " + id);
    }

    /**
     * Tüm siparişleri listeler.
     */
    @GetMapping
    @RequireRole(Role.STAFF)
    public List<Order> getAllOrders() {
        return orderService.listAllOrders();
    }
}
