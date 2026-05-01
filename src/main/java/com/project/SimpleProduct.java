package com.project.domain.product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.project.infrastructure.logger.SystemLogger;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Basit ürün - Composite Pattern'in yaprak (Leaf) düğümü.
 *
 * <p>Alt bileşenleri olmayan atomik ürünleri temsil eder (kalem, RAM çubuğu, vb.)
 * Observer Pattern ile stok gözlemcilerini barındırır.</p>
 */
@Entity
@Table(name = "simple_products")
public class SimpleProduct extends Product {

    private String sku;
    private double weightKg;

    @Transient
    private transient final SystemLogger logger = SystemLogger.getInstance();

    /**
     * JPA Varsayılan Yapıcı
     */
    protected SimpleProduct() {
        super();
    }

    /**
     * SimpleProduct constructor - Builder üzerinden çağrılmak üzere tasarlanmıştır.
     */
    public SimpleProduct(String name, String sku, double unitPrice,
                         double weightKg, int initialStock, int stockThreshold) {
        super();
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.weightKg = weightKg;
        this.stock = initialStock;
        this.stockThreshold = stockThreshold;
    }



    // --- Product Arayüzü Implementasyonu ---

    public double getWeightKg() { return weightKg; }
    public String getSku() { return sku; }

    @Override
    public void decreaseStock(int quantity) {
        if (quantity > this.stock) {
            throw new IllegalArgumentException(
                String.format("Yetersiz stok! '%s' için talep: %d, mevcut: %d", name, quantity, stock)
            );
        }
        this.stock -= quantity;
        logger.logCriticalOperation("STOCK_DECREASE",
            String.format("Ürün: '%s', Azalma: -%d, Yeni Stok: %d", name, quantity, stock));

        // AOP/Event-Driven yapı gereği event fırlatma işlemi Service katmanından (ApplicationEventPublisher) yapılacaktır.
        if (this.stock <= stockThreshold) {
            logger.warn(String.format("STOK EŞİK UYARISI: '%s' eşik altına düştü (%d <= %d)",
                name, stock, stockThreshold));
        }
    }

    @Override
    public void increaseStock(int quantity) {
        this.stock += quantity;
        logger.logCriticalOperation("STOCK_INCREASE",
            String.format("Ürün: '%s', Artış: +%d, Yeni Stok: %d", name, quantity, stock));
    }

    @Override
    public void display(String indent) {
        System.out.printf("%s[Ürün] %s | SKU: %s | Fiyat: %.2f TL | Stok: %d | Ağırlık: %.2f kg%n",
            indent, name, sku, unitPrice, stock, weightKg);
    }

    @Override
    public String toString() {
        return String.format("SimpleProduct{name='%s', stock=%d}", name, stock);
    }
}
