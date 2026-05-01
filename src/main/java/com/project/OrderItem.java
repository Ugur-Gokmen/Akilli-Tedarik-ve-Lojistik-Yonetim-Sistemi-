package com.project.domain.order;

import com.project.domain.product.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Sipariş kalemi - bir siparişte yer alan ürün ve miktarını temsil eder.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    private int quantity;
    private double unitPriceAtOrderTime; // Sipariş anındaki fiyat (değişmez)

    protected OrderItem() {}

    /**
     * @param product  Sipariş edilen ürün
     * @param quantity Sipariş edilen miktar
     */
    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPriceAtOrderTime = product.getUnitPrice();
    }

    /**
     * @return Bu kalemin toplam tutarı
     */
    public double getTotalPrice() {
        return unitPriceAtOrderTime * quantity;
    }

    public Long getId() { return id; }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getUnitPriceAtOrderTime() { return unitPriceAtOrderTime; }

    @Override
    public String toString() {
        return String.format("OrderItem{product='%s', qty=%d, price=%.2f TL}",
            product.getName(), quantity, getTotalPrice());
    }
}
