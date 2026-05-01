package com.project.controller;

import com.project.domain.order.Order;
import com.project.domain.payment.PaymentResult;
import com.project.domain.payment.PaymentStrategy;
import com.project.service.OrderService;
import com.project.service.Services.PaymentService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentWebController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ApplicationContext context; // Stratejileri dinamik çekmek için

    public PaymentWebController(PaymentService paymentService, OrderService orderService, ApplicationContext context) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.context = context;
    }

    /**
     * Ödeme sayfasını gösterir.
     */
    @GetMapping("/checkout/{orderId}")
    public String showPaymentPage(@PathVariable String orderId, Model model) {
        Order order = orderService.findById(orderId).orElseThrow();
        model.addAttribute("order", order);
        return "payment_form";
    }

    /**
     * Ödeme işlemini gerçekleştirir.
     * Strategy Pattern: Kullanıcının seçtiği 'strategyBean' ismine göre ilgili nesne tetiklenir.
     */
    @PostMapping("/process")
    public String processPayment(@RequestParam String orderId,
                                 @RequestParam String strategyBean,
                                 @RequestParam Map<String, String> details,
                                 RedirectAttributes redirectAttributes) {
        
        Order order = orderService.findById(orderId).orElseThrow();
        
        // Spring Context üzerinden ilgili stratejiyi (CreditCard, Crypto vb.) buluyoruz
        PaymentStrategy strategy = context.getBean(strategyBean, PaymentStrategy.class);
        
        // Ödemeyi gerçekleştir
        PaymentResult result = paymentService.processPayment(order, strategy, details);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", "Ödeme başarıyla alındı! Yöntem: " + result.getPaymentMethod());
            return "redirect:/orders/manage";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Ödeme başarısız: " + result.getErrorMessage());
            return "redirect:/payment/checkout/" + orderId;
        }
    }
}
