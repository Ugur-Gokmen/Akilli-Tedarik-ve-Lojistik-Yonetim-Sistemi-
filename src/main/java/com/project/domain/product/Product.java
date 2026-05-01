package com.project.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

/**
 * Ürün arayüzü (Abstract Class) - Composite Pattern'in bileşeni.
 *
 * <p>Composite Pattern: Hem basit ürünler (kalem, klavye) hem de karmaşık/
 * montaj ürünleri (bilgisayar kasası = CPU + RAM + PSU + ...) aynı arayüz
 * üzerinden işlenebilir. İstemci kodu, ürünün basit mi bileşik mi olduğunu
 * bilmek zorunda kalmaz.</p>
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Product {

    @Id
    protected String id;

    protected String name;
    protected double unitPrice;
    protected int stock;
    protected int stockThreshold;

    protected Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getStockThreshold() { return stockThreshold; }
    public void setStockThreshold(int stockThreshold) { this.stockThreshold = stockThreshold; }

    /**
     * Stok miktarını azaltır (satış/sevkiyat için).
     *
     * @param quantity Azaltılacak miktar
     * @throws IllegalArgumentException Yetersiz stok durumunda
     */
    public abstract void decreaseStock(int quantity);

    /**
     * Stok miktarını artırır (satın alma/iade için).
     *
     * @param quantity Artırılacak miktar
     */
    public abstract void increaseStock(int quantity);

    /**
     * Ürün detaylarını yazdırır (debug/raporlama için).
     *
     * @param indent Girintileme (composite ürünler için)
     */
    public abstract void display(String indent);
}
