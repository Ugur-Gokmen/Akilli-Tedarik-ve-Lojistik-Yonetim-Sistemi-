package com.project.domain.notification;


import com.project.domain.notification.event.StockLowEvent;
import com.project.infrastructure.logger.SystemLogger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Sistem içi bildirim gözlemcisi (Spring Event Listener).
 *
 * <p>Depo sorumlusuna, stok eşik altına düştüğünde sistem içi
 * (in-app) bildirim gönderir.</p>
 * 
 * SUNUM NOTU:
 * @EventListener kullanarak SADECE bir olayı beklediğini belirtiyoruz. 
 * Observer Pattern'in bu yeni modern yapısı (Pub/Sub) sayesinde 
 * Ürün ve Bildirim sınıfları arasında hiçbir doğrudan bağlantı (coupling) kalmadı!
 */
@Component
public class InAppStockNotifier {

    private final String warehouseManagerName = "Depo Sorumlusu Ahmet";
    private final SystemLogger logger = SystemLogger.getInstance();

    @EventListener
    public void onStockBelowThreshold(StockLowEvent event) {
        String message = String.format(
            "🔔 SİSTEM BİLDİRİMİ → [%s] | Ürün: '%s' kritik stok seviyesine düştü! " +
            "Mevcut: %d adet | Eşik: %d adet | Lütfen depoya göz atın.",
            warehouseManagerName, event.getProductName(), event.getCurrentStock(), event.getThreshold()
        );
        logger.logCriticalOperation("INAPP_NOTIFICATION", message);
        System.out.println("   >> " + message);
    }
}
