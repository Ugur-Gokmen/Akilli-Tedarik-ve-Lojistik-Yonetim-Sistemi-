package com.project.domain.notification.event;

import org.springframework.context.ApplicationEvent;

/**
 * Spring Event-Driven mimarisi için Stok Düşüş Olayı.
 *
 * <p>Stok eşik altına düştüğünde yayınlanan domain olayı; ilgili payload bilgilerini taşır.</p>
 */
public class StockLowEvent extends ApplicationEvent {
    
    private final String productId;
    private final String productName;
    private final int currentStock;
    private final int threshold;

    public StockLowEvent(Object source, String productId, String productName, int currentStock, int threshold) {
        super(source);
        this.productId = productId;
        this.productName = productName;
        this.currentStock = currentStock;
        this.threshold = threshold;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getCurrentStock() { return currentStock; }
    public int getThreshold() { return threshold; }
}
