package com.project.domain.notification;

import com.project.domain.product.Product;

/**
 * Stok gözlemcisi arayüzü - Observer Pattern.
 *
 * <p>Observer Pattern: Bir ürünün stoğu eşik değerin altına düştüğünde
 * bu arayüzü implement eden tüm gözlemciler (Satın Alma, Depo Sorumlusu vb.)
 * otomatik olarak bilgilendirilir.</p>
 *
 * <p>Yeni bir bildirim kanalı eklemek için sadece bu arayüzü implement etmek yeterlidir;
 * mevcut kod değiştirilmez → Open/Closed Principle.</p>
 */
public interface StockObserver {

    /**
     * Stok eşik altına düştüğünde çağrılır.
     *
     * @param product       Stoğu azalan ürün
     * @param currentStock  Mevcut stok miktarı
     * @param threshold     Eşik değer
     */
    void onStockBelowThreshold(Product product, int currentStock, int threshold);
}
