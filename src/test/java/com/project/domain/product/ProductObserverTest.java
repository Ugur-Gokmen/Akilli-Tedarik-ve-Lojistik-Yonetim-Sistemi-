package com.project.domain.product;

import com.project.domain.notification.StockObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Product Observer Tests - QA Analysis")
class ProductObserverTest {

    @Test
    @DisplayName("Stok eşiğin altına düştüğünde kayıtlı tüm observer'lar notify edilmelidir")
    void shouldNotifyObserversWhenStockDropsBelowThreshold() {
        // Arrange
        SimpleProduct product = new SimpleProduct("Monitor", "SKU-MON", 1000.0, 5.0, 10, 5);
        StockObserver mockEmailObserver = Mockito.mock(StockObserver.class);
        StockObserver mockSystemObserver = Mockito.mock(StockObserver.class);

        product.addObserver(mockEmailObserver);
        product.addObserver(mockSystemObserver);

        // Act
        product.decreaseStock(6); // Stok 10 - 6 = 4 kaldı. Eşik (5) aşıldı!

        // Assert - verify interaction
        Mockito.verify(mockEmailObserver, Mockito.times(1)).update(product);
        Mockito.verify(mockSystemObserver, Mockito.times(1)).update(product);
    }
}
