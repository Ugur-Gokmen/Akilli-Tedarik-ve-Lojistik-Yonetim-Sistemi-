package com.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.domain.notification.StockObserver;
import com.project.domain.product.Product;
import com.project.domain.product.SimpleProduct;
import com.project.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Testleri")
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockObserver stockObserver;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(productRepository);
    }

    @Test
    @DisplayName("Aynı SKU ile ürün eklenemez")
    void givenExistingSku_whenAddProduct_thenThrowsIllegalArgumentException() {
        SimpleProduct existing = new SimpleProduct("Kalem", "SKU-100", 10.0, 0.1, 20, 5);
        SimpleProduct candidate = new SimpleProduct("Yeni Kalem", "SKU-100", 12.0, 0.1, 25, 5);

        when(productRepository.findBySku("SKU-100")).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> inventoryService.addProduct(candidate));

        assertEquals("Bu SKU'ya sahip başka bir ürün zaten var: SKU-100", ex.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Yeni ürün başarıyla kaydedilir")
    void givenUniqueSku_whenAddProduct_thenSavesProduct() {
        SimpleProduct product = new SimpleProduct("Defter", "SKU-200", 20.0, 0.3, 30, 5);

        when(productRepository.findBySku("SKU-200")).thenReturn(Optional.empty());

        inventoryService.addProduct(product);

        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Restock miktarı pozitif olmalıdır")
    void givenNonPositiveQuantity_whenRestock_thenThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> inventoryService.restockProduct("P-1", 0));

        assertEquals("Stok artırımı için pozitif miktar girilmeli! Girilen: 0", ex.getMessage());
    }

    @Test
    @DisplayName("Restock stok miktarını artırır ve kaydeder")
    void givenExistingProduct_whenRestock_thenIncreasesStockAndSaves() {
        SimpleProduct product = new SimpleProduct("SSD", "SSD-22", 1500.0, 0.2, 10, 2);
        int oldStock = product.getStock();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        inventoryService.restockProduct(product.getId(), 5);

        assertEquals(oldStock + 5, product.getStock());
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("SimpleProduct için observer eklenebilir")
    void givenSimpleProduct_whenAddStockObserver_thenObserverIsAdded() {
        SimpleProduct product = new SimpleProduct("Mouse", "M-10", 300.0, 0.1, 15, 5);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        inventoryService.addStockObserver(product.getId(), stockObserver);

        assertNotNull(stockObserver);
        verify(productRepository).findById(product.getId());
    }

    @Test
    @DisplayName("Ürün bulunamazsa observer ekleme hatası verir")
    void givenMissingProduct_whenAddStockObserver_thenThrowsIllegalArgumentException() {
        when(productRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> inventoryService.addStockObserver("UNKNOWN", stockObserver));

        assertEquals("Ürün bulunamadı: UNKNOWN", ex.getMessage());
    }
}
