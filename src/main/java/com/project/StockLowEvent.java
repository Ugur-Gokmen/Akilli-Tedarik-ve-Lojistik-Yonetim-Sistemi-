package com.project.domain.notification.event;

import org.springframework.context.ApplicationEvent;

/**
 * Spring Event-Driven mimarisi için Stok Düşüş Olayı.
 * 
 * SUNUM NOTU:
 * Event-Driven (Olay Güdümlü) mimaride gerçekleşen olayın içeriğini
 * (Payload) taşıyan sınıftır. Gözlemcilere ulaşması gereken "hangi ürün?" 
 * veya "stok ne kadar kaldı?" gibi bilgiler bu paketin içine konur.
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
