package com.project.domain.notification;


import com.project.domain.notification.event.StockLowEvent;
import com.project.infrastructure.logger.SystemLogger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * E-posta bildirim gözlemcisi (Spring Event Listener).
 *
 * <p>Satın Alma birimine, stok eşik altına düştüğünde e-posta gönderir.</p>
 */
@Component
public class EmailStockNotifier {

    private final String recipientEmail = "purchasing@sirket.com";
    private final SystemLogger logger = SystemLogger.getInstance();

    @EventListener
    public void onStockBelowThreshold(StockLowEvent event) {
        String message = String.format(
            "📧 E-POSTA GÖNDERİLDİ → [%s] | Konu: DÜŞÜK STOK UYARISI | " +
            "Ürün: '%s' | Mevcut Stok: %d | Eşik: %d | Lütfen satın alma sürecini başlatın.",
            recipientEmail, event.getProductName(), event.getCurrentStock(), event.getThreshold()
        );
        logger.logCriticalOperation("EMAIL_NOTIFICATION", message);
        System.out.println("   >> " + message);
    }
}
