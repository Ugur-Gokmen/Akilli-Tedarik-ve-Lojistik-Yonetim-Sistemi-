package com.project.controller;

import com.project.domain.cargo.CargoProvider;
import com.project.domain.order.Order;
import com.project.infrastructure.factory.CargoProviderFactory;
import com.project.service.OrderService;
import com.project.service.Services.CargoService;
import com.project.infrastructure.security.RequireRole;
import com.project.domain.user.Role;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cargo")
public class CargoWebController {

    private final CargoService cargoService;
    private final OrderService orderService;

    public CargoWebController(CargoService cargoService, OrderService orderService) {
        this.cargoService = cargoService;
        this.orderService = orderService;
    }

    /**
     * Siparişi kargolama işlemini (Form Submit) yakalar.
     * STAFF yetkisi gerektirir [AOP Security].
     */
    @PostMapping("/ship/{orderId}")
    @RequireRole(Role.STAFF)
    public String processShipping(
            @PathVariable String orderId,
            @RequestParam CargoProviderFactory.CargoCompany company,
            @RequestParam String senderCity,
            @RequestParam String receiverCity,
            @RequestParam double distanceKm,
            @RequestParam(defaultValue = "false") boolean withInsurance,
            @RequestParam(defaultValue = "false") boolean withFragile,
            RedirectAttributes redirectAttributes) {

        // 1. Siparişi bul
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı"));

        // 2. Factory Pattern: Kullanıcının seçtiği firmaya göre doğru Adapter'i üret
        CargoProvider provider = CargoProviderFactory.create(company);

        // 3. Decorator Pattern: Fiyatı dinamik olarak hesapla
        double cost = cargoService.calculateShippingCost(
                provider, order, distanceKm, withInsurance, withFragile);

        // 4. Adapter Pattern: Takip numarası oluştur (Her firmanın API'si farklıdır)
        String trackingNo = cargoService.generateAndAssignTracking(
                provider, order, senderCity, receiverCity);

        // 5. State Pattern: Sipariş durumunu "Kargoda" olarak güncelle
        orderService.shipOrder(order.getId(), trackingNo, cost);

        // Başarı mesajını View'a taşı
        String message = String.format("Sipariş başarıyla %s firmasına verildi! Takip No: %s | Tutar: %.2f TL",
                provider.getCompanyName(), trackingNo, cost);
        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/orders/manage";
    }
}
