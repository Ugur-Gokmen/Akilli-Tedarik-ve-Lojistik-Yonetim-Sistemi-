package com.project.controller;

import com.project.service.InventoryApplicationService;
import com.project.domain.user.Role;
import com.project.ui.SessionManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/inventory")
public class InventoryWebController {

    private final InventoryApplicationService inventoryApplicationService;
    private final SessionManager sessionManager;

    public InventoryWebController(InventoryApplicationService inventoryApplicationService, SessionManager sessionManager) {
        this.inventoryApplicationService = inventoryApplicationService;
        this.sessionManager = sessionManager;
    }

    @ModelAttribute("currentUser")
    public com.project.domain.user.User currentUser() {
        return sessionManager.getCurrentUser();
    }

    // 1. Ürünleri Listeleme
    @GetMapping("/list")
    public String listProducts(@RequestParam(required = false) String q,
                               @RequestParam(required = false) String stock,
                               Model model) {
        com.project.domain.user.User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.CUSTOMER) {
            return "redirect:/catalog";
        }

        List<com.project.domain.product.Product> allProducts = inventoryApplicationService.listAllProducts();
        List<com.project.domain.product.Product> filtered = new ArrayList<>(allProducts);

        String normalizedQuery = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        if (!normalizedQuery.isBlank()) {
            filtered = filtered.stream()
                .filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                    || p.getId().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                    || (p instanceof com.project.domain.product.SimpleProduct sp
                        && sp.getSku().toLowerCase(Locale.ROOT).contains(normalizedQuery)))
                .toList();
        }

        if ("critical".equalsIgnoreCase(stock)) {
            filtered = filtered.stream()
                .filter(p -> p.getStock() <= p.getStockThreshold())
                .toList();
        } else if ("healthy".equalsIgnoreCase(stock)) {
            filtered = filtered.stream()
                .filter(p -> p.getStock() > p.getStockThreshold())
                .toList();
        }

        model.addAttribute("products", filtered);
        model.addAttribute("totalProductCount", allProducts.size());
        model.addAttribute("activeQuery", q == null ? "" : q.trim());
        model.addAttribute("activeStock", stock == null ? "all" : stock.toLowerCase(Locale.ROOT));
        return "products";
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
        inventoryApplicationService.addSimpleProduct(name, sku, price, weight, stock, threshold);
        return "redirect:/inventory/list";
    }

    // 3. Bileşik Ürün Ekleme Sayfası
    @GetMapping("/add-composite")
    public String showCompositeForm(Model model) {
        // Alt bileşen seçebilmek için mevcut tüm ürünleri gönderiyoruz
        model.addAttribute("availableProducts", inventoryApplicationService.listAllProducts());
        return "add_composite";
    }

    @PostMapping("/add-composite")
    public String addCompositeProduct(@RequestParam String name, @RequestParam double assemblyFee,
                                      @RequestParam int stock, @RequestParam int threshold,
                                      @RequestParam List<String> selectedComponentIds) {
        inventoryApplicationService.addCompositeProduct(name, assemblyFee, stock, threshold, selectedComponentIds);
        return "redirect:/inventory/list";
    }
}
