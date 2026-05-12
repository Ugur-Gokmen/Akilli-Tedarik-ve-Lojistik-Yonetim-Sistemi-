package com.project.api.controller;

import com.project.domain.product.Product;
import com.project.service.InventoryApplicationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryRestController {

    private final InventoryApplicationService inventoryApplicationService;

    public InventoryRestController(InventoryApplicationService inventoryApplicationService) {
        this.inventoryApplicationService = inventoryApplicationService;
    }

    /**
     * ConsoleApp 137. satırda 'listProducts()' isminde bir metot bekliyor.
     */
    @GetMapping("/list")
    public List<Product> listProducts() {
        return inventoryApplicationService.listAllProducts();
    }

    /**
     * ConsoleApp 316. satırda 'listLowStockProducts()' bekliyor.
     */
    @GetMapping("/reports/low-stock")
    public List<Product> listLowStockProducts() {
        return inventoryApplicationService.lowStockProducts();
    }

    /**
     * ConsoleApp 329. satırda 'addProduct(Product)' bekliyor.
     */
    @PostMapping("/add")
    public void addProduct(@RequestBody Product product) {
        inventoryApplicationService.addProductFromApi(product);
    }
}