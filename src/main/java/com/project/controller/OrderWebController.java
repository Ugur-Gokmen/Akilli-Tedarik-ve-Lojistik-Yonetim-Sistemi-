package com.project.controller;

import com.project.domain.order.Order;
import com.project.infrastructure.factory.CargoProviderFactory.CargoCompany;
import com.project.domain.user.Role;
import com.project.service.OrderManagementService;
import com.project.service.OrderManagementService.OrderManagementSnapshot;
import com.project.service.OrderService;
import com.project.ui.SessionManager;
import com.project.ui.ShoppingCart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderWebController {

    private final OrderService orderService;
    private final OrderManagementService orderManagementService;
    private final SessionManager sessionManager;
    private final ShoppingCart shoppingCart;

    public OrderWebController(OrderService orderService,
                              OrderManagementService orderManagementService,
                              SessionManager sessionManager,
                              ShoppingCart shoppingCart) {
        this.orderService = orderService;
        this.orderManagementService = orderManagementService;
        this.sessionManager = sessionManager;
        this.shoppingCart = shoppingCart;
    }

    @ModelAttribute("currentUser")
    public com.project.domain.user.User currentUser() {
        return sessionManager.getCurrentUser();
    }

    @ModelAttribute("cartCount")
    public int cartCount() {
        return shoppingCart.getTotalItems();
    }

    // Tüm siparişleri listeleme (Staff/Admin)
    @GetMapping("/manage")
    public String manageOrders(Model model) {
        OrderManagementSnapshot snapshot = orderManagementService.buildSnapshot();
        model.addAttribute("orders", snapshot.orders());
        model.addAttribute("orderUiMap", snapshot.orders().stream()
            .collect(Collectors.toMap(Order::getId, Function.identity(), (a, b) -> a))
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> toUi(e.getValue()))));
        model.addAttribute("pendingCount", snapshot.pendingCount());
        model.addAttribute("approvedCount", snapshot.approvedCount());
        model.addAttribute("preparingCount", snapshot.preparingCount());
        model.addAttribute("shippedCount", snapshot.shippedCount());
        model.addAttribute("deliveredCount", snapshot.deliveredCount());
        
        return "order_management";
    }

    // Müşterinin kendi siparişleri
    @GetMapping("/my-orders")
    public String myOrders(Model model) {
        com.project.domain.user.User user = sessionManager.getCurrentUser();
        if (user != null) {
            model.addAttribute("orders", orderService.listCustomerOrders(user));
        }
        return "my_orders";
    }

    @PostMapping("/quick-order")
    public String quickOrder(@RequestParam String productId,
                             @RequestParam(defaultValue = "1") int quantity,
                             RedirectAttributes ra) {
        com.project.domain.user.User user = sessionManager.getCurrentUser();
        if (user == null) {
            ra.addFlashAttribute("errorMessage", "Sipariş için önce giriş yapmalısınız.");
            return "redirect:/auth/login";
        }
        if (user.getRole() != Role.CUSTOMER) {
            ra.addFlashAttribute("errorMessage", "Hızlı sipariş yalnızca müşteri hesapları için aktif.");
            return "redirect:/inventory/list";
        }
        try {
            Order order = orderService.createOrder(user);
            orderService.addItemToOrder(order.getId(), productId, quantity);
            ra.addFlashAttribute("successMessage", "Sipariş oluşturuldu. Ödeme adımına geçebilirsiniz.");
            return "redirect:/payment/" + order.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Sipariş oluşturulamadı: " + e.getMessage());
            return "redirect:/inventory/list";
        }
    }

    // --- Durum Geçişleri ---

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable String id, RedirectAttributes ra) {
        try {
            orderService.approveOrder(id);
            ra.addFlashAttribute("successMessage", "Sipariş onaylandı.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Onay hatası: " + e.getMessage());
        }
        return "redirect:/orders/manage";
    }

    @PostMapping("/{id}/prepare")
    public String prepare(@PathVariable String id, RedirectAttributes ra) {
        try {
            orderService.startPreparing(id);
            ra.addFlashAttribute("successMessage", "Hazırlık süreci başlatıldı.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Hata: " + e.getMessage());
        }
        return "redirect:/orders/manage";
    }

    @PostMapping("/{id}/deliver")
    public String deliver(@PathVariable String id, RedirectAttributes ra) {
        try {
            orderService.deliverOrder(id);
            ra.addFlashAttribute("successMessage", "Sipariş teslim edildi olarak işaretlendi.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Hata: " + e.getMessage());
        }
        return "redirect:/orders/manage";
    }

    @PostMapping("/{id}/return")
    public String returnOrder(@PathVariable String id, RedirectAttributes ra) {
        try {
            orderService.returnOrder(id);
            ra.addFlashAttribute("successMessage", "İade süreci başlatıldı.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Hata: " + e.getMessage());
        }
        // Eğer müşteri ise kendi siparişlerine, staff ise yönetime dönsün
        if (sessionManager.getCurrentUser().isStaffOrAbove()) {
            return "redirect:/orders/manage";
        }
        return "redirect:/orders/my-orders";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable String id, RedirectAttributes ra) {
        try {
            orderService.cancelOrder(id);
            ra.addFlashAttribute("successMessage", "Sipariş iptal edildi.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Hata: " + e.getMessage());
        }
        if (sessionManager.getCurrentUser().isStaffOrAbove()) {
            return "redirect:/orders/manage";
        }
        return "redirect:/orders/my-orders";
    }

    // Lojistik Sayfası (Shipment Form)
    @GetMapping("/{id}/shipping-form")
    public String shippingForm(@PathVariable String id, Model model) {
        model.addAttribute("order", orderService.findById(id).orElseThrow());
        model.addAttribute("companies", CargoCompany.values());
        return "shipping_form";
    }

    private OrderUi toUi(Order order) {
        String state = order.getCurrentStateName();
        String badgeClass = switch (state) {
            case "BEKLEMEDE" -> "bg-warning/10 text-warning border border-warning/20";
            case "TESLİM EDİLDİ" -> "bg-success/10 text-success border border-success/20";
            case "İPTAL EDİLDİ" -> "bg-error/10 text-error border border-error/20";
            default -> "bg-primary/10 text-primary border border-primary/20";
        };
        return new OrderUi(
            badgeClass,
            "BEKLEMEDE".equals(state),
            "ONAYLANDI".equals(state),
            "HAZIRLANIYOR".equals(state),
            "KARGODA".equals(state),
            "KARGODA".equals(state) || "TESLİM EDİLDİ".equals(state)
        );
    }

    public record OrderUi(
        String badgeClass,
        boolean canApprove,
        boolean canPrepare,
        boolean canShip,
        boolean canDeliver,
        boolean canReturn
    ) {}
}
