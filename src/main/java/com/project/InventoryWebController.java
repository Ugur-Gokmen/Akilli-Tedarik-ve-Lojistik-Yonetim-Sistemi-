package com.project.controller;

import com.project.domain.product.CompositeProduct;
import com.project.domain.product.Product;
import com.project.domain.product.SimpleProduct;
import com.project.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryWebController {

    private final InventoryService inventoryService;

    public InventoryWebController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // 1. Ürünleri Listeleme
    @GetMapping("/list")
    public String listProducts(Model model) {
        model.addAttribute("products", inventoryService.listAllProducts());
        return "inventory_list";
    }

    // 2. Basit Ürün Ekleme Sayfası
    @GetMapping("/add-simple")
    public String showSimpleForm() {
        return "add_simple";
    }

    @PostMapping("/add-simple")
    public String addSimpleProduct(@RequestParam String name, @RequestParam String sku,
                                   @RequestParam double price, @RequestParam double weight,
                                   @RequestParam int stock, @RequestParam int threshold) {
        SimpleProduct product = new SimpleProduct(name, sku, price, weight, stock, threshold);
        inventoryService.addProduct(product);
        return "redirect:/inventory/list";
    }

    // 3. Bileşik Ürün Ekleme Sayfası
    @GetMapping("/add-composite")
    public String showCompositeForm(Model model) {
        // Alt bileşen seçebilmek için mevcut tüm ürünleri gönderiyoruz
        model.addAttribute("availableProducts", inventoryService.listAllProducts());
        return "add_composite";
    }

    @PostMapping("/add-composite")
    public String addCompositeProduct(@RequestParam String name, @RequestParam double assemblyFee,
                                      @RequestParam int stock, @RequestParam int threshold,
                                      @RequestParam List<String> selectedComponentIds) {
        CompositeProduct.Builder builder = new CompositeProduct.Builder(name)
                .assemblyFee(assemblyFee)
                .initialStock(stock)
                .stockThreshold(threshold);

        for (String id : selectedComponentIds) {
            inventoryService.findById(id).ifPresent(builder::addComponent);
        }

        inventoryService.addProduct(builder.build()); // Builder Pattern kullanımı
        return "redirect:/inventory/list";
    }
}
