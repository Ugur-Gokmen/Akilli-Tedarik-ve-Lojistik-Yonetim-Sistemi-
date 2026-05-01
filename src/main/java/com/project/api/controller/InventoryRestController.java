package com.project.api.controller;

import com.project.domain.product.Product;
import com.project.service.InventoryService; // Servis isminiz farklıysa güncelleyin
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
     * ConsoleApp 137. satırda 'listProducts()' isminde bir metot bekliyor.
     */
    @GetMapping("/list")
    public List<Product> listProducts() {
        List<Product> products = inventoryService.getAllProducts();
        // Konsola çıktı vermek isterseniz:
        products.forEach(p -> p.display(""));
        return products;
    }

    /**
     * ConsoleApp 316. satırda 'listLowStockProducts()' bekliyor.
     */
    @GetMapping("/reports/low-stock")
    public void listLowStockProducts() {
        inventoryService.getLowStockProducts().forEach(p -> p.display("  [DÜŞÜK STOK] "));
    }

    /**
     * ConsoleApp 329. satırda 'addProduct(Product)' bekliyor.
     */
    @PostMapping("/add")
    public void addProduct(@RequestBody Product product) {
        inventoryService.saveProduct(product);
    }
}