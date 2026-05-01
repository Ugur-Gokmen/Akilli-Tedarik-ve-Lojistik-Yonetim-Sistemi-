package com.project.domain.order;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.project.domain.user.User;
import com.project.infrastructure.logger.SystemLogger;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;

    private LocalDateTime createdAt;
    private String trackingNumber;
    private double shippingCost;

    // [KRİTİK]: State Pattern - Tekil referans ve JPA Converter
    @Convert(converter = OrderStateConverter.class)
    private OrderState currentState;

    @Transient
    private transient final SystemLogger logger = SystemLogger.getInstance();

    protected Order() {}

    public Order(User customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Sipariş için müşteri (customer) null olamaz.");
        }
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.customer = customer;
        this.items = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        // Başlangıç durumu: OrderStates içindeki statik sınıfa erişim
        this.currentState = new OrderStates.PendingState(); 
        logger.info("Yeni sipariş oluşturuldu: " + this.id + " | Müşteri: " + customer.getUsername());
    }

    // --- State Geçiş Metodları (Delegasyon) ---

    public void approve()        { currentState.approve(this); }
    public void startPreparing() { currentState.startPreparing(this); }
    public void ship()           { currentState.ship(this); }
    public void deliver()        { currentState.deliver(this); }
    public void returnOrder()    { currentState.returnOrder(this); }
    public void cancel()         { currentState.cancel(this); }

    /**
     * [HATA ÇÖZÜMÜ]: Sequential (Ardışık) geçiş desteği.
     * OrderService içindeki çağrılar için handleNext metoduna delege eder.
     */
    public void nextState() {
        if (currentState != null) {
            currentState.handleNext(this);
        } else {
            logger.error("Hata: Mevcut durum null, geçiş yapılamıyor! Sipariş ID: " + id);
        }
    }

    /**
     * State nesnelerinin durumu değiştirmek için kullandığı setter.
     */
    public void setState(OrderState state) {
        this.currentState = state;
        logger.info(String.format("Sipariş [%s] durum değiştirdi -> %s", id, state.getStateName()));
    }

    // --- Ürün ve Finans Yönetimi ---

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Eklenecek sipariş kalemi null olamaz.");
        }
        
        // [SENIOR DEFENSIVE CHECK]: Yalnızca PENDING aşamasında ürün eklenebilir
        if (!(currentState instanceof OrderStates.PendingState)) {
            throw new IllegalStateException("Sadece beklemede (Pending) olan siparişlere ürün eklenebilir. Mevcut: " + currentState.getStateName());
        }

        items.add(item);
        item.getProduct().decreaseStock(item.getQuantity());
        logger.info(String.format("Siparişe ürün eklendi: %s x%d", item.getProduct().getName(), item.getQuantity()));
    }

    public double getTotalAmount() {
        return items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    public double getGrandTotal() {
        return getTotalAmount() + shippingCost;
    }

    public double getTotalWeightKg() {
        return items.stream()
            .mapToDouble(item -> {
                if (item.getProduct() instanceof com.project.domain.product.SimpleProduct sp) {
                    return sp.getWeightKg() * item.getQuantity();
                } else if (item.getProduct() instanceof com.project.domain.product.CompositeProduct cp) {
                    return cp.getTotalWeightKg() * item.getQuantity();
                }
                return 0;
            }).sum();
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public User getCustomer() { return customer; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCurrentStateName() { return currentState.getStateName(); }
    public OrderState getCurrentState() { return currentState; }
    public String getTrackingNumber() { return trackingNumber; }
    public double getShippingCost() { return shippingCost; }

    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public void setShippingCost(double shippingCost) { this.shippingCost = shippingCost; }

    @Override
    public String toString() {
        return String.format("Order{id='%s', state='%s', total=%.2f TL}", id, getCurrentStateName(), getGrandTotal());
    }
}