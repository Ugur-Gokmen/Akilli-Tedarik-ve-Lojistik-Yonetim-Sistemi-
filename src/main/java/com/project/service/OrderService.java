package com.project.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.domain.order.Order;
import com.project.domain.order.OrderItem;
import com.project.domain.order.OrderStates; 
import com.project.domain.product.Product;
import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.domain.payment.PaymentStrategy;
import com.project.domain.payment.PaymentResult;
import com.project.domain.notification.event.StockLowEvent;
import com.project.infrastructure.logger.SystemLogger;
import com.project.infrastructure.security.RequireRole;
import com.project.repository.OrderRepository;
import com.project.repository.ProductRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SystemLogger logger = SystemLogger.getInstance();

    @Autowired
    public OrderService(OrderRepository orderRepository, 
                        ProductRepository productRepository, 
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order createOrder(User customer) {
        if (customer == null) throw new IllegalArgumentException("Müşteri null olamaz.");
        Order order = new Order(customer);
        return orderRepository.save(order);
    }

    @Transactional
    public void addItemToOrder(String orderId, String productId, int quantity) {
        Order order = findOrderOrThrow(orderId);
        Product product = findProductOrThrow(productId);
        
        OrderItem item = new OrderItem(product, quantity);
        order.addItem(item);
        
        if (product.getStock() <= product.getStockThreshold()) {
            eventPublisher.publishEvent(new StockLowEvent(this, product.getId(), 
                product.getName(), product.getStock(), product.getStockThreshold()));
        }
        orderRepository.save(order);
    }

    @RequireRole(Role.STAFF)
    @Transactional
    public void approveOrder(String orderId) {
        Order order = findOrderOrThrow(orderId);
        order.approve();
        orderRepository.save(order);
    }
    
    /**
     * Siparişi teslim edildi olarak işaretler.
     * @param orderId Sipariş ID'si
     */
    public void deliverOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı: " + orderId));
        
        // Domain metodunu çağır (State Pattern devreye girer)
        order.deliver(); 
        
        orderRepository.save(order);
        System.out.println("Sipariş teslim edildi: " + orderId);
    }
    
    @RequireRole(Role.STAFF)
    @Transactional
    public Optional<Order> findById(String id) {
        return orderRepository.findById(id);
    }

    public void startPreparing(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı"));
        // State Pattern: Siparişi hazırlık aşamasına geçir
        order.nextState(); 
        orderRepository.save(order);
    }

    // Hata Çözümü: pay(Order, double, Map) parametre uyumu
    public void processOrderPayment(Order order, PaymentStrategy strategy, Map<String, String> details) {
        // Stratejiye 'order' nesnesini de gönderiyoruz
        strategy.pay(order, order.getTotalAmount(), details); 
    }

    // Hata Çözümü: shipOrder imza uyumu (Sadece id, tracking ve cost alan versiyon)
    public void shipOrder(String orderId, String trackingNumber, double shippingCost) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı"));
        order.setTrackingNumber(trackingNumber);
        order.setShippingCost(shippingCost);
        order.nextState(); // SHIPPED durumuna geçer
        orderRepository.save(order);
    }

    @Transactional
    public PaymentResult pay(String orderId, PaymentStrategy strategy, Map<String, String> details) {
        Order order = findOrderOrThrow(orderId);
        PaymentResult result = strategy.pay(order,order.getTotalAmount(), details);

        if (result.isSuccess()) {
            // Ödeme başarılıysa durumu APPROVED yap
            order.setState(new com.project.domain.order.OrderStates.ApprovedState());
            orderRepository.save(order);
        }
        return result;
    }

    @RequireRole(Role.STAFF)
    public List<Order> listAllOrders() {
        return orderRepository.findAll();
    }

    private Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı: " + orderId));
    }

    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı: " + productId));
    }
}