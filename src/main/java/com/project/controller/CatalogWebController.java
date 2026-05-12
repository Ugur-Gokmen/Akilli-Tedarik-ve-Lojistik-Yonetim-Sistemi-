package com.project.controller;

import java.util.List;
import java.util.Locale;
import java.util.Comparator;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.domain.order.Order;
import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.service.InventoryApplicationService;
import com.project.service.InventoryService;
import com.project.service.OrderService;
import com.project.ui.SessionManager;
import com.project.ui.ShoppingCart;

@Controller
@RequestMapping
public class CatalogWebController {

    private final InventoryApplicationService inventoryApplicationService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final SessionManager sessionManager;
    private final ShoppingCart shoppingCart;

    public CatalogWebController(InventoryApplicationService inventoryApplicationService,
                                InventoryService inventoryService,
                                OrderService orderService,
                                SessionManager sessionManager,
                                ShoppingCart shoppingCart) {
        this.inventoryApplicationService = inventoryApplicationService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.sessionManager = sessionManager;
        this.shoppingCart = shoppingCart;
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        return sessionManager.getCurrentUser();
    }

    @ModelAttribute("cartCount")
    public int cartCount() {
        return shoppingCart.getTotalItems();
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(required = false) String q, Model model) {
        User user = requireCustomer();
        List<com.project.domain.product.Product> products = inventoryApplicationService.listAllProducts();
        int totalProductCount = products.size();
        if (q != null && !q.isBlank()) {
            String normalized = q.trim().toLowerCase(Locale.ROOT);
            products = products.stream()
                .filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(normalized)
                    || p.getId().toLowerCase(Locale.ROOT).contains(normalized)
                    || (p instanceof com.project.domain.product.SimpleProduct sp
                        && sp.getSku().toLowerCase(Locale.ROOT).contains(normalized)))
                .toList();
        }
        products = products.stream()
            .sorted(Comparator
                .comparingInt((com.project.domain.product.Product p) -> p.getStock() <= 0 ? 1 : 0)
                .thenComparing(com.project.domain.product.Product::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
        model.addAttribute("products", products);
        model.addAttribute("totalProductCount", totalProductCount);
        model.addAttribute("activeQuery", q == null ? "" : q.trim());
        model.addAttribute("customerName", user.getUsername());
        return "catalog";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes ra) {
        requireCustomer();
        try {
            com.project.domain.product.Product product = inventoryService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı."));
            shoppingCart.add(product, quantity);
            ra.addFlashAttribute("successMessage", "Ürün sepete eklendi.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Sepete eklenemedi: " + e.getMessage());
        }
        return "redirect:/catalog";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        requireCustomer();
        model.addAttribute("cart", shoppingCart);
        return "cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam String productId,
                             @RequestParam int quantity,
                             RedirectAttributes ra) {
        requireCustomer();
        try {
            shoppingCart.updateQuantity(productId, quantity);
            ra.addFlashAttribute("successMessage", "Sepet güncellendi.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Sepet güncellenemedi: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable String productId) {
        requireCustomer();
        shoppingCart.remove(productId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(RedirectAttributes ra) {
        User user = requireCustomer();
        if (shoppingCart.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Sepetiniz boş.");
            return "redirect:/cart";
        }
        try {
            Order order = orderService.createOrder(user);
            for (ShoppingCart.CartLine line : shoppingCart.getLines()) {
                orderService.addItemToOrder(order.getId(), line.getProductId(), line.getQuantity());
            }
            shoppingCart.clear();
            return "redirect:/checkout/" + order.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Sipariş oluşturulamadı: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/checkout/{orderId}")
    public String checkoutSummary(@PathVariable String orderId, Model model) {
        User user = requireCustomer();
        Order order = orderService.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı."));
        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new SecurityException("Bu sipariş size ait değil.");
        }
        model.addAttribute("order", order);
        return "checkout_summary";
    }

    private User requireCustomer() {
        User user = sessionManager.getCurrentUser();
        if (user == null) {
            throw new SecurityException("Bu işlem için giriş yapmalısınız.");
        }
        if (user.getRole() != Role.CUSTOMER) {
            throw new SecurityException("Bu ekran sadece müşteri kullanıcıları içindir.");
        }
        return user;
    }
}
