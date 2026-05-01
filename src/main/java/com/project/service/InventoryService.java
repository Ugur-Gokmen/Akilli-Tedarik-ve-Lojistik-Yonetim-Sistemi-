package com.project.service;

import com.project.domain.notification.StockObserver;
import com.project.domain.product.Product;
import com.project.domain.product.SimpleProduct;
import com.project.infrastructure.logger.SystemLogger;
import com.project.infrastructure.security.RequireRole;
import com.project.domain.user.Role;
import com.project.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Envanter yönetim servisi.
 *
 * <p>Ürün ekleme, stok güncelleme ve gözlemci yönetimini koordine eder.
 * Yetkilendirme AOP (@RequireRole + SecurityAspect) ile sağlanır —
 * bu sınıfta hiçbir if-else yetki kontrolü bulunmaz.</p>
 */
@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final SystemLogger logger = SystemLogger.getInstance();

    @Autowired
    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ─────────────────────────────────────────────────
    // Ürün Yönetimi
    // ─────────────────────────────────────────────────

    /**
     * Sisteme yeni ürün ekler (STAFF / ADMIN yetkisi gerektirir).
     *
     * @param product Eklenecek ürün
     */
    @RequireRole(Role.STAFF)
    @Transactional
    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Kaydedilecek ürün null olamaz.");
        }
        productRepository.save(product);
        logger.logCriticalOperation("PRODUCT_ADD",
            String.format("Yeni ürün eklendi: '%s' | ID: %s", product.getName(), product.getId()));
        System.out.printf("✅ Ürün eklendi: %s (Stok: %d)%n", product.getName(), product.getStock());
    }

    /**
     * REST controller'lardan gelen çağrılar için alias.
     * addProduct() ile aynı işlevi görür.
     *
     * @param product Kaydedilecek ürün
     */
    @RequireRole(Role.STAFF)
    @Transactional
    public void saveProduct(Product product) {
        addProduct(product);
    }

    /**
     * Ürün stokunu artırır.
     *
     * @param productId Ürün ID'si
     * @param quantity  Artırılacak miktar (pozitif olmalı)
     */
    @RequireRole(Role.STAFF)
    @Transactional
    public void restockProduct(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Stok artırımı için pozitif miktar girilmeli! Girilen: " + quantity);
        }
        Product product = findOrThrow(productId);
        product.increaseStock(quantity);
        productRepository.save(product);
        System.out.printf("📦 Stok güncellendi: %s | +%d → Toplam: %d%n",
            product.getName(), quantity, product.getStock());
    }

    /**
     * SimpleProduct'a stok gözlemcisi ekler (Observer Pattern).
     *
     * @param productId Ürün ID'si
     * @param observer  Eklenecek gözlemci
     */
    @RequireRole(Role.STAFF)
    public void addStockObserver(String productId, StockObserver observer) {
        Product product = findOrThrow(productId);
        if (product instanceof SimpleProduct sp) {
            sp.addObserver(observer);
            logger.info("Gözlemci eklendi: '" + product.getName() + "' → " +
                observer.getClass().getSimpleName());
        } else {
            throw new IllegalArgumentException(
                "Gözlemci yalnızca SimpleProduct türüne eklenebilir: " + productId);
        }
    }

    // ─────────────────────────────────────────────────
    // Sorgulama
    // ─────────────────────────────────────────────────

    /**
     * Tüm ürünleri listeler.
     *
     * @return Ürün listesi
     */
    public List<Product> listAllProducts() {
        return productRepository.findAll();
    }

    /**
     * REST controller'lar için alias — listAllProducts() ile aynı.
     *
     * @return Ürün listesi
     */
    public List<Product> getAllProducts() {
        return listAllProducts();
    }

    /**
     * Stok eşiği altındaki kritik ürünleri listeler.
     *
     * @return Kritik stok ürünleri
     */
    @RequireRole(Role.STAFF)
    public List<Product> getLowStockProducts() {
        return productRepository.findBelowThreshold();
    }

    /**
     * ID ile ürün arar.
     *
     * @param productId Ürün ID'si
     * @return Ürün (varsa)
     */
    public Optional<Product> findById(String productId) {
        return productRepository.findById(productId);
    }

    // ─────────────────────────────────────────────────
    // Yardımcı
    // ─────────────────────────────────────────────────

    private Product findOrThrow(String productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı: " + productId));
    }
}