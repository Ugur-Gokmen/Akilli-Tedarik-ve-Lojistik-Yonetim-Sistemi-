package com.project.domain.notification;

import com.project.domain.product.SimpleProduct;

/**
 * Stok gözlemcisi arayüzü - Observer Pattern.
 */
public interface StockObserver {
    /**
     * Stok eşik altına düştüğünde çağrılır.
     * Parametre olarak SimpleProduct almak, tüm stok ve eşik bilgilerine 
     * nesne üzerinden erişilmesini sağlar.
     */
    void update(SimpleProduct product);
}
