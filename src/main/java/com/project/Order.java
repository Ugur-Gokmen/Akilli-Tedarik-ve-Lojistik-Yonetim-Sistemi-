package com.project.domain.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import com.project.domain.user.User;
import com.project.infrastructure.logger.SystemLogger;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.*;

/**
 * Sipariş domain entity'si - State Pattern'in Context sınıfı.
 *
 * <p>Order nesnesi mevcut durumunu bir OrderState referansı olarak tutar.
 * Tüm durum geçiş operasyonları ilgili state nesnesine delege edilir.
 * Order sınıfının kendisi hiçbir if-else veya switch-case içermez.</p>
 */
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

    // State Pattern: mevcut durum referansı
    @Convert(converter = OrderStateConverter.class)
    private OrderState currentState;

    @Transient
    private transient final SystemLogger logger = SystemLogger.getInstance();

    protected Order() {}

    /**
     * Yeni sipariş oluşturur. Başlangıç durumu: PendingState.
     *
     * @param customer Siparişi veren müşteri
     */
    public Order(User customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Sipariş için müşteri (customer) null olamaz.");
        }
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.customer = customer;
        this.items = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.currentState = new OrderStates.PendingState(); // Başlangıç durumu
        logger.info("Yeni sipariş oluşturuldu: " + this.id + " | Müşteri: " + customer.getUsername());
    }

    // --- State Geçiş Metodları (State'e delege edilir) ---

    /** Siparişi onaylar */
    public void approve()        { currentState.approve(this); }

    /** Hazırlamayı başlatır */
    public void startPreparing() { currentState.startPreparing(this); }

    /** Kargoya verir */
    public void ship()           { currentState.ship(this); }

    /** Teslim edildi olarak işaretler */
    public void deliver()        { currentState.deliver(this); }

    /** İade sürecini başlatır */
    public void returnOrder()    { currentState.returnOrder(this); }

    /** Siparişi iptal eder */
    public void cancel()         { currentState.cancel(this); }

    // --- Paket içi durum setter'ı (sadece State sınıfları kullanır) ---

    /**
     * Durumu değiştirir. Sadece OrderState implementasyonları çağırır.
     *
     * @param newState Yeni durum
     */
    void setState(OrderState newState) {
        logger.info(String.format("Sipariş %s: %s → %s",
            id, currentState.getStateName(), newState.getStateName()));
        this.currentState = newState;
    }

    // --- Ürün Yönetimi ---

    /**
     * Siparişe kalem ekler ve stoktan düşer.
     *
     * @param item Eklenecek sipariş kalemi
     */
    public void addItem(OrderItem item) {
        // [SENIOR DEFENSIVE CHECK] 1: Boş ürün nesnesi kontrolü
        if (item == null) {
            throw new IllegalArgumentException("Eklenecek sipariş kalemi null olamaz.");
        }
        
        // [SENIOR DEFENSIVE CHECK] 2: State kontrolü - Yalnızca beklemedeki siparişe ürün eklenebilir
        if (!(currentState instanceof OrderStates.PendingState)) {
            throw new IllegalStateException("Sadece beklemede (Pending) olan siparişlere yeni ürün eklenebilir. Mevcut durum: " + currentState.getStateName());
        }

        items.add(item);
        // Stoktan düş - Observer tetiklenebilir
        item.getProduct().decreaseStock(item.getQuantity());
        logger.info(String.format("Siparişe ürün eklendi: %s x%d → Sipariş: %s",
            item.getProduct().getName(), item.getQuantity(), id));
    }

    /**
     * @return Sipariş toplam tutarı (kargo dahil değil)
     */
    public double getTotalAmount() {
        return items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    /**
     * @return Sipariş toplam tutarı + kargo ücreti
     */
    public double getGrandTotal() {
        return getTotalAmount() + shippingCost;
    }

    /**
     * @return Sipariş toplam ağırlığı (kg)
     */
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

    // --- Getters ---

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
        return String.format("Order{id='%s', customer='%s', state='%s', total=%.2f TL}",
            id, customer.getUsername(), getCurrentStateName(), getGrandTotal());
    }
}
