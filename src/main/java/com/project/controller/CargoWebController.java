package com.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.domain.cargo.CargoProvider;
import com.project.domain.order.Order;
import com.project.domain.user.Role;
import com.project.infrastructure.factory.CargoProviderFactory;
import com.project.infrastructure.security.RequireRole;
import com.project.service.OrderService;
import com.project.service.Services.CargoService;

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
    @PostMapping("/ship")
    public String shipOrder(@RequestParam String orderId, 
                             @RequestParam CargoProviderFactory.CargoCompany company,
                             @RequestParam String senderCity,
                             @RequestParam String receiverCity,
                             @RequestParam double distanceKm,
                             @RequestParam(defaultValue = "false") boolean withInsurance,
                             @RequestParam(defaultValue = "false") boolean withFragile) {
        
        // 1. Siparişi bul
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı: " + orderId));

        // 2. Factory kullanarak kargo sağlayıcısını al
        CargoProvider provider = CargoProviderFactory.getProvider(company);

        // 3. Kargo hesaplamalarını CargoService üzerinden yap (Controller mantığı değil, delege ediyoruz)
        String trackingNumber = cargoService.generateAndAssignTracking(
                provider, order, senderCity, receiverCity);
        
        double cost = cargoService.calculateShippingCost(
                provider, order, distanceKm, withInsurance, withFragile);

        // 4. [ÇÖZÜM]: OrderService'in beklediği 3 parametreli imzayı kullanıyoruz
        // (orderId, trackingNumber, shippingCost)
        orderService.shipOrder(orderId, trackingNumber, cost);

        return "redirect:/orders/view?id=" + orderId;
    }
}
