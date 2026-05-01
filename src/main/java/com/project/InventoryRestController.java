package com.project.api;

import com.project.api.dto.CompositeProductRequest;
import com.project.api.dto.SimpleProductRequest;
import com.project.domain.product.CompositeProduct;
import com.project.domain.product.Product;
import com.project.domain.product.SimpleProduct;
import com.project.domain.user.Role;
import com.project.infrastructure.security.RequireRole;
import com.project.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryRestController {

    private final InventoryService inventoryService;

    public InventoryRestController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Tüm ürünleri listeler.
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(inventoryService.listAllProducts());
    }

    /**
     * Yeni bir basit ürün (SimpleProduct) ekler.
     */
    @PostMapping("/products/simple")
    @RequireRole(Role.STAFF) // AOP Güvenlik Katmanı
    public ResponseEntity<String> addSimpleProduct(@RequestBody SimpleProductRequest request) {
        
        SimpleProduct product = new SimpleProduct(
            request.name(), request.sku(), request.unitPrice(),
            request.weightKg(), request.initialStock(), request.stockThreshold()
        );
        
        inventoryService.addProduct(product);
        return ResponseEntity.ok("Basit ürün başarıyla eklendi: " + product.getName());
    }

    /**
     * Yeni bir bileşik ürün (CompositeProduct) ekler.
     * Builder Design Pattern burada DTO verileriyle beslenir!
     */
    @PostMapping("/products/composite")
    @RequireRole(Role.STAFF)
    public ResponseEntity<String> addCompositeProduct(@RequestBody CompositeProductRequest request) {
        
        // Builder'ı başlatıyoruz
        CompositeProduct.Builder builder = new CompositeProduct.Builder(request.name())
                .assemblyFee(request.assemblyFee())
                .initialStock(request.initialStock())
                .stockThreshold(request.stockThreshold());

        // Gelen ID'lere göre veritabanından alt ürünleri bulup Builder'a ekliyoruz
        for (String compId : request.componentIds()) {
            Product component = inventoryService.findById(compId)
                    .orElseThrow(() -> new IllegalArgumentException("Bileşen bulunamadı: " + compId));
            builder.addComponent(component);
        }

        // Builder pattern ile ürünü oluştur ve kaydet
        CompositeProduct compositeProduct = builder.build();
        inventoryService.addProduct(compositeProduct);

        return ResponseEntity.ok("Bileşik ürün başarıyla eklendi: " + compositeProduct.getName());
    }

    /**
     * Stok günceller.
     */
    @PutMapping("/products/{id}/restock")
    @RequireRole(Role.STAFF)
    public ResponseEntity<String> restockProduct(@PathVariable String id, @RequestParam int quantity) {
        inventoryService.restockProduct(id, quantity);
        return ResponseEntity.ok("Stok güncellendi: " + id + ", Eklendi: " + quantity);
    }
}
