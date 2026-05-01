package com.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.domain.notification.StockObserver;
import com.project.domain.product.Product;
import com.project.domain.product.SimpleProduct;
import com.project.domain.user.Role;
import com.project.infrastructure.logger.SystemLogger;
import com.project.infrastructure.security.RequireRole;
import com.project.repository.ProductRepository;

/**
 * Envanter yönetim servisi.
 *
 * <p>Ürün ekleme, stok güncelleme ve gözlemci yönetimini koordine eder.
 * Yetkilendirme kontrolü AOP üzerinden yapılır.</p>
 */
@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final SystemLogger logger = SystemLogger.getInstance();

    @Autowired
    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Tüm ürünleri listeler. 
     * Controller (getAllProducts) ve ConsoleApp için ortak erişim noktası.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * listAllProducts ismini de geriye dönük uyumluluk için tutuyoruz.
     */
    public List<Product> listAllProducts() {
        return getAllProducts();
    }

    /**
     * Sisteme yeni ürün ekler (STAFF veya ADMIN yetkisi gerektirir).
     */
    @RequireRole(Role.STAFF)
    @Transactional
    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Veritabanına eklenecek ürün null olamaz.");
        }
        productRepository.save(product);
        logger.logCriticalOperation("PRODUCT_ADD",
            String.format("Yeni ürün eklendi: '%s' | ID: %s", product.getName(), product.getId()));
        System.out.printf("✅ Ürün eklendi: %s (Stok: %d)%n", product.getName(), product.getStock());
    }

    /**
     * Ürünü kaydeder (Controller içindeki genel çağrılar için).
     */
    @Transactional
    public void saveProduct(Product product) {
        if (product != null) {
            productRepository.save(product);
        }
    }

    /**
     * Ürün stokunu artırır.
     */
    @RequireRole(Role.STAFF)
    @Transactional
    public void restockProduct(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stok artırımı için pozitif bir miktar girilmelidir!");
        }

        Product product = findOrThrow(productId);
        product.increaseStock(quantity);
        productRepository.save(product);
        System.out.printf("📦 Stok güncellendi: %s | +%d → Toplam: %d%n",
            product.getName(), quantity, product.getStock());
    }

    /**
     * Stok eşiği altındaki ürünleri listeler.
     */
    @RequireRole(Role.STAFF)
    public List<Product> getLowStockProducts() {
        // Repository'nizde findBelowThreshold metodu tanımlı olmalıdır.
        // Eğer yoksa: return productRepository.findAll().stream().filter(p -> p.getStock() <= p.getStockThreshold()).toList();
        return productRepository.findBelowThreshold();
    }

    /**
     * SimpleProduct'a stok gözlemcisi ekler (Observer Pattern).
     */
    @RequireRole(Role.STAFF)
    public void addStockObserver(String productId, StockObserver observer) {
        Product product = findOrThrow(productId);
        if (product instanceof SimpleProduct sp) {
            sp.addObserver(observer);
            logger.info(String.format("Gözlemci eklendi: Ürün '%s'", product.getName()));
        } else {
            throw new IllegalArgumentException("Gözlemci sadece basit ürünlere eklenebilir.");
        }
    }

    public Optional<Product> findById(String productId) {
        return productRepository.findById(productId);
    }

    private Product findOrThrow(String productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı: " + productId));
    }
}