package com.project.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.project.infrastructure.logger.SystemLogger;
import com.project.domain.notification.StockObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "simple_products")
public class SimpleProduct extends Product {

    private static final Logger log = LoggerFactory.getLogger(SimpleProduct.class);
    @Column(unique = true)
    private String sku;
    private double weightKg;

    @Transient
    private final List<StockObserver> observers = new ArrayList<>();

    @Transient
    private transient final SystemLogger logger = SystemLogger.getInstance();

    protected SimpleProduct() {
        super();
    }

    public SimpleProduct(String name, String sku, double unitPrice,
                         double weightKg, int initialStock, int stockThreshold) {
        super();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ürün adı boş olamaz.");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU boş olamaz.");
        }
        if (stockThreshold < 0 || stockThreshold >= initialStock) {
            throw new IllegalArgumentException("Stok eşiği 0'dan küçük veya başlangıç stoğundan büyük/eşit olamaz.");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.weightKg = weightKg;
        this.stock = initialStock;
        this.stockThreshold = stockThreshold;
    }

    // --- Observer Pattern Metotları ---

    /**
     * InventoryService tarafından çağrılan, yeni gözlemci ekleme metodu.
     */
    public void addObserver(StockObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Kayıtlı tüm gözlemcileri stok değişikliği konusunda bilgilendirir.
     */
    private void notifyObservers() {
        for (StockObserver observer : observers) {
            // Artık StockObserver içindeki 'update' metodunu çağırıyoruz
            observer.update(this); 
        }
    }

    // --- Product Arayüzü Implementasyonu ---

    public double getWeightKg() { return weightKg; }
    public String getSku() { return sku; }

    @Override
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Azaltılacak miktar pozitif olmalıdır! Girilen: " + quantity);
        }
        if (quantity > this.stock) {
            throw new IllegalArgumentException(
                String.format("Yetersiz stok! '%s' için talep: %d, mevcut: %d", name, quantity, stock)
            );
        }
        this.stock -= quantity;
        logger.logCriticalOperation("STOCK_DECREASE",
            String.format("Ürün: '%s', Azalma: -%d, Yeni Stok: %d", name, quantity, stock));

        // Stok kritik eşiğin altına düştüğünde gözlemcileri haberdar et
        if (this.stock <= stockThreshold) {
            logger.warn(String.format("STOK EŞİK UYARISI: '%s' eşik altına düştü (%d <= %d)",
                name, stock, stockThreshold));
            
            notifyObservers(); // Observer Pattern tetikleniyor
        }
    }

    @Override
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Artırılacak miktar pozitif olmalıdır! Girilen: " + quantity);
        }
        this.stock += quantity;
        logger.logCriticalOperation("STOCK_INCREASE",
            String.format("Ürün: '%s', Artış: +%d, Yeni Stok: %d", name, quantity, stock));
    }

    @Override
    public void display(String indent) {
        log.info("{}[Ürün] {} | SKU: {} | Fiyat: {} TL | Stok: {} | Ağırlık: {} kg",
            indent, name, sku, unitPrice, stock, weightKg);
    }

    @Override
    public String toString() {
        return String.format("SimpleProduct{name='%s', stock=%d}", name, stock);
    }

    @Override
    public boolean isComposite() {
        return false;
    }
}