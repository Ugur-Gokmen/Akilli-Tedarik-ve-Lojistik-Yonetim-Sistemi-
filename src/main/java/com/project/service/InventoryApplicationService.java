package com.project.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.project.domain.product.CompositeProduct;
import com.project.domain.product.SimpleProduct;

@Service
public class InventoryApplicationService {

    private final InventoryService inventoryService;

    public InventoryApplicationService(InventoryService inventoryService) {
        this.inventoryService = Objects.requireNonNull(inventoryService, "inventoryService");
    }

    public void addSimpleProduct(String name,
                                 String sku,
                                 double price,
                                 double weight,
                                 int stock,
                                 int threshold) {
        SimpleProduct product = new SimpleProduct(name, sku, price, weight, stock, threshold);
        inventoryService.addProduct(product);
    }

    public void addCompositeProduct(String name,
                                    double assemblyFee,
                                    int stock,
                                    int threshold,
                                    List<String> selectedComponentIds) {
        CompositeProduct.Builder builder = new CompositeProduct.Builder(name)
            .assemblyFee(assemblyFee)
            .initialStock(stock)
            .stockThreshold(threshold);

        if (selectedComponentIds != null) {
            for (String id : selectedComponentIds) {
                inventoryService.findById(id).ifPresent(builder::addComponent);
            }
        }

        inventoryService.addProduct(builder.build());
    }

    public List<com.project.domain.product.Product> listAllProducts() {
        return inventoryService.listAllProducts();
    }

    public List<com.project.domain.product.Product> lowStockProducts() {
        return inventoryService.getLowStockProducts();
    }

    public void addProductFromApi(com.project.domain.product.Product product) {
        inventoryService.saveProduct(product);
    }
}

