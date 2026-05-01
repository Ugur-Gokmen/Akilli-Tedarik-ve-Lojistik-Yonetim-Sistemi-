package com.project.domain.product;

import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.project.infrastructure.logger.SystemLogger;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Bileşik ürün - Composite Pattern'in Composite düğümü.
 *
 * <p>Alt bileşenler içeren montaj ürünlerini temsil eder.
 * Örnek: Bilgisayar Kasası = CPU + RAM + SSD + PSU + Anakart</p>
 *
 * <p>Builder Pattern ile oluşturulur; parametre sayısı çok fazla olduğu için
 * telescoping constructor anti-pattern'inden kaçınılır.</p>
 */
@Entity
@Table(name = "composite_products")
public class CompositeProduct extends Product {

    private double assemblyFee;  // Montaj ücreti

    // Composite: alt bileşen listesi (her biri bir Product olabilir)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "composite_components",
        joinColumns = @JoinColumn(name = "composite_id"),
        inverseJoinColumns = @JoinColumn(name = "component_id")
    )
    private List<Product> components;

    @Transient
    private transient final SystemLogger logger = SystemLogger.getInstance();

    /**
     * JPA Varsayılan Yapıcı
     */
    protected CompositeProduct() {
        super();
    }

    /**
     * Private constructor - sadece Builder üzerinden oluşturulur.
     */
    private CompositeProduct(Builder builder) {
        super();
        this.id = UUID.randomUUID().toString();
        this.name = builder.name;
        this.assemblyFee = builder.assemblyFee;
        this.stock = builder.initialStock;
        this.stockThreshold = builder.stockThreshold;
        this.components = new ArrayList<>(builder.components);
    }

    /**
     * Toplam ağırlık: tüm bileşen ağırlıklarının toplamı.
     *
     * @return Toplam ağırlık (kg)
     */
    public double getTotalWeightKg() {
        return components.stream()
            .filter(p -> p instanceof SimpleProduct)
            .mapToDouble(p -> ((SimpleProduct) p).getWeightKg())
            .sum();
    }


    /**
     * Toplam fiyat: bileşenlerin fiyatları toplamı + montaj ücreti.
     */
    @Override
    public double getUnitPrice() {
        double componentTotal = components.stream()
            .mapToDouble(Product::getUnitPrice)
            .sum();
        return componentTotal + assemblyFee;
    }


    @Override
    public void decreaseStock(int quantity) {
        if (quantity > this.stock) {
            throw new IllegalArgumentException(
                String.format("Yetersiz stok! '%s' için talep: %d, mevcut: %d", name, quantity, stock)
            );
        }
        this.stock -= quantity;
        logger.logCriticalOperation("STOCK_DECREASE",
            String.format("Bileşik Ürün: '%s', Azalma: -%d, Yeni Stok: %d", name, quantity, stock));
    }

    @Override
    public void increaseStock(int quantity) {
        this.stock += quantity;
        logger.logCriticalOperation("STOCK_INCREASE",
            String.format("Bileşik Ürün: '%s', Artış: +%d, Yeni Stok: %d", name, quantity, stock));
    }

    @Override
    public void display(String indent) {
        System.out.printf("%s[Bileşik Ürün] %s | Montaj: %.2f TL | Toplam: %.2f TL | Stok: %d%n",
            indent, name, assemblyFee, getUnitPrice(), stock);
        System.out.println(indent + "  └─ Bileşenler:");
        for (Product component : components) {
            component.display(indent + "     ");
        }
    }

    public List<Product> getComponents() {
        return List.copyOf(components);
    }

    // =========================================================
    // Builder Pattern - Karmaşık ürün oluşturmak için
    // =========================================================

    /**
     * CompositeProduct oluşturucu.
     *
     * <p>Builder Pattern: Çok sayıda parametreye sahip CompositeProduct
     * nesnelerini adım adım, okunabilir bir şekilde oluşturmayı sağlar.
     * Telescoping constructor anti-pattern'ini ortadan kaldırır.</p>
     *
     * <pre>
     * Kullanım:
     *   CompositeProduct bilgisayar = new CompositeProduct.Builder("Gaming PC")
     *       .assemblyFee(250.0)
     *       .addComponent(cpu)
     *       .addComponent(ram)
     *       .addComponent(ssd)
     *       .initialStock(10)
     *       .stockThreshold(2)
     *       .build();
     * </pre>
     */
    public static class Builder {
        private final String name;
        private double assemblyFee = 0.0;
        private int initialStock = 0;
        private int stockThreshold = 5;
        private final List<Product> components = new ArrayList<>();

        /**
         * @param name Bileşik ürün adı
         */
        public Builder(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Ürün adı boş olamaz.");
            }
            this.name = name;
        }

        /**
         * Montaj ücreti ayarlar.
         *
         * @param fee Montaj ücreti (TL)
         * @return Builder zinciri
         */
        public Builder assemblyFee(double fee) {
            this.assemblyFee = fee;
            return this;
        }

        /**
         * Bileşen ekler. Aynı metot birden fazla çağrılabilir.
         *
         * @param component Eklenecek ürün bileşeni
         * @return Builder zinciri
         */
        public Builder addComponent(Product component) {
            this.components.add(component);
            return this;
        }

        /**
         * Başlangıç stok miktarını ayarlar.
         *
         * @param stock Başlangıç stoğu
         * @return Builder zinciri
         */
        public Builder initialStock(int stock) {
            this.initialStock = stock;
            return this;
        }

        /**
         * Stok uyarı eşiğini ayarlar.
         *
         * @param threshold Eşik değer
         * @return Builder zinciri
         */
        public Builder stockThreshold(int threshold) {
            this.stockThreshold = threshold;
            return this;
        }

        /**
         * CompositeProduct nesnesini oluşturur ve döner.
         *
         * @return Oluşturulan CompositeProduct
         * @throws IllegalStateException Bileşen yoksa
         */
        public CompositeProduct build() {
            if (components.isEmpty()) {
                throw new IllegalStateException(
                    "Bileşik ürün en az 1 bileşen içermelidir: " + name);
            }
            return new CompositeProduct(this);
        }
    }
}
