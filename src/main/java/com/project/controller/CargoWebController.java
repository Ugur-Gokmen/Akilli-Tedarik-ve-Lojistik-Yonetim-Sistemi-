package com.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.infrastructure.factory.CargoProviderFactory;
import com.project.service.ShippingApplicationService;
import com.project.ui.SessionManager;

@Controller
@RequestMapping("/cargo")
public class CargoWebController {

    private final ShippingApplicationService shippingApplicationService;
    private final SessionManager sessionManager;

    public CargoWebController(ShippingApplicationService shippingApplicationService,
                              SessionManager sessionManager) {
        this.shippingApplicationService = shippingApplicationService;
        this.sessionManager = sessionManager;
    }

    @ModelAttribute("currentUser")
    public com.project.domain.user.User currentUser() {
        return sessionManager.getCurrentUser();
    }

    /**
     * Siparişi kargolama işlemini (Form Submit) yakalar.
     * Şablon: th:action="@{'/cargo/ship/' + ${order.id}}"
     */
    @PostMapping("/ship/{orderId}")
    public String shipOrder(@PathVariable String orderId, 
                             @RequestParam CargoProviderFactory.CargoCompany company,
                             @RequestParam String senderCity,
                             @RequestParam String receiverCity,
                             @RequestParam double distanceKm,
                             @RequestParam(defaultValue = "false") boolean withInsurance,
                             @RequestParam(defaultValue = "false") boolean withFragile,
                             RedirectAttributes redirectAttrs) {
        
        try {
            var result = shippingApplicationService.shipOrder(
                orderId, company, senderCity, receiverCity, distanceKm, withInsurance, withFragile);

            redirectAttrs.addFlashAttribute("successMessage",
                "Sipariş kargoya verildi! Takip No: " + result.trackingNumber());
            return "redirect:/orders/manage";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Kargo hatası: " + e.getMessage());
            return "redirect:/orders/" + orderId + "/shipping-form";
        }
    }
}
