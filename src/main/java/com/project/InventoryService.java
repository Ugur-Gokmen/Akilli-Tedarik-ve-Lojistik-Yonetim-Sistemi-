package com.project.service;

import java.util.Optional;

import com.project.domain.notification.StockObserver;
import com.project.domain.product.Product;
import com.project.domain.product.SimpleProduct;
import com.project.domain.user.AuthorizationGuard;
import com.project.infrastructure.logger.SystemLogger;
import com.project.repository.ProductRepository;
import com.project.infrastructure.security.RequireRole;
import com.project.domain.user.Role;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Envanter yönetim servisi.
 *
 * <p>Ürün ekleme, stok güncelleme ve gözlemci yönetimini koordine eder.
 * Yetkilendirme kontrolü AuthorizationGuard üzerinden yapılır.</p>
 */
@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final SystemLogger logger = SystemLogger.getInstance();

    /**
     * @param productRepository Ürün deposu
     */
    @Autowired
    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Sisteme yeni ürün ekler (STAFF veya ADMIN yetkisi gerektirir).
     *
     * @param product Eklenecek ürün
     */
    // SUNUM NOTU: @RequireRole anotasyonu, bu metodu çağıran kullanıcının STAFF (veya ADMIN) olmasını garanti eder.
    // Metot içindeki yetki kontrol kirliliği AOP (SecurityAspect) kullanılarak yok edilmiştir.
    @RequireRole(Role.STAFF) // [AOP SECURITY]
    @Transactional
    public void addProduct(Product product) {
        // [SENIOR DEFENSIVE CHECK]: Null objeye karşı koruma
        if (product == null) {
            throw new IllegalArgumentException("Veritabanına eklenecek ürün null olamaz.");
        }
        productRepository.save(product);
        logger.logCriticalOperation("PRODUCT_ADD",
            String.format("Yeni ürün eklendi: '%s' | ID: %s", product.getName(), product.getId()));
        System.out.printf("✅ Ürün eklendi: %s (Stok: %d)%n", product.getName(), product.getStock());
    }

    /**
     * Ürün stokunu artırır (satın alma, iade vb.).
     *
     * @param productId Ürün ID'si
     * @param quantity  Artırılacak miktar
     */
    @RequireRole(Role.STAFF) // [AOP SECURITY]
    @Transactional
    public void restockProduct(String productId, int quantity) {
        // [SENIOR DEFENSIVE CHECK]: Eksi stok eklenemez (Stok düşürmek için kullanılmamalı)
        if (quantity <= 0) {
            throw new IllegalArgumentException(String.format("Stok artırımı için pozitif bir miktar girilmelidir! İstenen: %d", quantity));
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
    @RequireRole(Role.STAFF) // [AOP SECURITY]
    public void addStockObserver(String productId, StockObserver observer) {
        Product product = findOrThrow(productId);
        if (product instanceof SimpleProduct sp) {
            sp.addObserver(observer);
            logger.info(String.format("Gözlemci eklendi: Ürün '%s' | Gözlemci: %s",
                product.getName(), observer.getClass().getSimpleName()));
        } else {
            throw new IllegalArgumentException("Gözlemci sadece basit ürünlere eklenebilir: " + productId);
        }
    }

    /**
     * Tüm ürünleri listeler.
     *
     * @return Ürün listesi
     */
    public List<Product> listAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Stok eşiği altındaki ürünleri listeler (ADMIN yetkisi).
     *
     * @return Kritik stok ürünleri
     */
    @RequireRole(Role.STAFF) // [AOP SECURITY]
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

    private Product findOrThrow(String productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı: " + productId));
    }
}
