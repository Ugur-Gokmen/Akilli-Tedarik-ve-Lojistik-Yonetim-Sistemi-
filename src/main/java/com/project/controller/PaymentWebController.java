package com.project.controller;

import com.project.domain.order.Order;
import com.project.domain.payment.PaymentResult;
import com.project.service.OrderService;
import com.project.service.PaymentApplicationService;
import com.project.ui.SessionManager;
import com.project.ui.ShoppingCart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentWebController {

    private final OrderService orderService;
    private final PaymentApplicationService paymentApplicationService;
    private final SessionManager sessionManager;
    private final ShoppingCart shoppingCart;

    public PaymentWebController(OrderService orderService,
                                PaymentApplicationService paymentApplicationService,
                                SessionManager sessionManager,
                                ShoppingCart shoppingCart) {
        this.orderService = orderService;
        this.paymentApplicationService = paymentApplicationService;
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

    /**
     * Ödeme sayfasını gösterir.
     */
    @GetMapping("/{orderId}")
    public String showPaymentPage(@PathVariable String orderId, Model model) {
        Order order = orderService.findById(orderId).orElseThrow();
        model.addAttribute("order", order);
        return "payment_form";
    }

    /**
     * Ödeme işlemini gerçekleştirir.
     */
    @PostMapping("/process")
    public String processPayment(@RequestParam String orderId,
                                 @RequestParam String strategyBean,
                                 @RequestParam Map<String, String> details,
                                 RedirectAttributes redirectAttributes) {
        if ("creditCardStrategy".equals(strategyBean)) {
            String cardNumber = details.getOrDefault("details['cardNumber']", "").replaceAll("\\s+", "");
            String expiry = details.getOrDefault("details['expiry']", "");
            String cvv = details.getOrDefault("details['cvv']", "");

            boolean hasFieldError = false;
            if (!cardNumber.matches("\\d{16}")) {
                redirectAttributes.addFlashAttribute("cardNumberError", "Kart numarası 16 haneli olmalıdır.");
                hasFieldError = true;
            }
            if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                redirectAttributes.addFlashAttribute("expiryError", "SKT MM/YY formatında olmalıdır.");
                hasFieldError = true;
            }
            if (!cvv.matches("\\d{3,4}")) {
                redirectAttributes.addFlashAttribute("cvvError", "CVV 3 veya 4 haneli olmalıdır.");
                hasFieldError = true;
            }
            if (hasFieldError) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lütfen ödeme alanlarındaki hataları düzeltin.");
                return "redirect:/payment/" + orderId;
            }
        }

        try {
            PaymentResult result = paymentApplicationService.processPayment(orderId, strategyBean, details);

            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("successMessage", "Ödeme başarıyla alındı! Yöntem: " + result.getPaymentMethod());
                return "redirect:/orders/my-orders";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Ödeme başarısız: " + result.getErrorMessage());
                return "redirect:/payment/" + orderId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Beklenmedik bir hata oluştu: " + e.getMessage());
            return "redirect:/payment/" + orderId;
        }
    }
}
