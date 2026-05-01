package com.project.controller;

import com.project.domain.order.Order;
import com.project.infrastructure.factory.CargoProviderFactory.CargoCompany;
import com.project.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class OrderWebController {

    private final OrderService orderService;

    public OrderWebController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Tüm siparişleri listeleme
    @GetMapping("/manage")
    public String manageOrders(Model model) {
        model.addAttribute("orders", orderService.listAllOrders());
        return "order_management";
    }

    // Durum Geçişleri (State Pattern Delegasyonu)
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable String id) {
        orderService.approveOrder(id);
        return "redirect:/orders/manage";
    }

    @PostMapping("/{id}/prepare")
    public String prepare(@PathVariable String id) {
        orderService.startPreparing(id);
        return "redirect:/orders/manage";
    }

    // Lojistik Sayfası (Shipment Form)
    @GetMapping("/{id}/shipping-form")
    public String shippingForm(@PathVariable String id, Model model) {
        model.addAttribute("order", orderService.findById(id).orElseThrow());
        model.addAttribute("companies", CargoCompany.values());
        return "shipping_form";
    }
}
