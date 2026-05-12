package com.project.domain.notification;


import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.domain.notification.event.StockLowEvent;
import com.project.config.AppProperties;
import com.project.infrastructure.logger.SystemLogger;

/**
 * E-posta bildirim gözlemcisi (Spring Event Listener).
 *
 * <p>Satın Alma birimine, stok eşik altına düştüğünde e-posta gönderir.</p>
 */
@Component
public class EmailStockNotifier {

    private static final Logger log = LoggerFactory.getLogger(EmailStockNotifier.class);
    private final AppProperties appProperties;
    private final SystemLogger logger = SystemLogger.getInstance();

    public EmailStockNotifier(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @EventListener
    public void onStockBelowThreshold(StockLowEvent event) {
        String recipientEmail = appProperties.getNotifications().getStock().getLowRecipientEmail();
        String message = String.format(
            "📧 E-POSTA GÖNDERİLDİ → [%s] | Konu: DÜŞÜK STOK UYARISI | " +
            "Ürün: '%s' | Mevcut Stok: %d | Eşik: %d | Lütfen satın alma sürecini başlatın.",
            recipientEmail, event.getProductName(), event.getCurrentStock(), event.getThreshold()
        );
        logger.logCriticalOperation("EMAIL_NOTIFICATION", message);
        log.info(message);
    }
}
