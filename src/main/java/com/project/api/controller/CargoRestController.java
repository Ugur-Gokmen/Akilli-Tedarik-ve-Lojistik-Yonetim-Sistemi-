package com.project.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.api.dto.ShippingRequest;
import com.project.domain.cargo.CargoProvider;
import com.project.domain.order.Order;
import com.project.infrastructure.factory.CargoProviderFactory;
import com.project.service.OrderService;
import com.project.service.Services.CargoService;

/**
 * REST Controller - Kargo işlemlerini dış dünyaya (API) açan katman.
 * DTO (Record) kullanımı ile veri güvenliği ve immutability sağlanmıştır.
 */
@RestController
@RequestMapping("/api/cargo")
public class CargoRestController {

    private final CargoService cargoService;
    private final OrderService orderService;

    @Autowired
    public CargoRestController(CargoService cargoService, OrderService orderService) {
        this.cargoService = cargoService;
        this.orderService = orderService;
    }

    /**
     * Siparişi kargoya verir ve maliyet hesaplar.
     * Record erişimi: request.withInsurance() (get/is ön eki yoktur).
     */
    @PostMapping("/ship")
    public ResponseEntity<?> shipOrder(@RequestBody ShippingRequest request) {
        Order order = orderService.findById(request.orderId())
            .orElseThrow(() -> new RuntimeException("Sipariş yok"));

        // ÇÖZÜM: Enum'ı Factory kullanarak gerçek Provider nesnesine dönüştür
        CargoProvider provider = CargoProviderFactory.getProvider(request.company());

        // generateAndAssignTracking artık doğru tipte (CargoProvider) argüman alıyor
        String tracking = cargoService.generateAndAssignTracking(
            provider, order, request.senderCity(), request.receiverCity()
        );

        double cost = cargoService.calculateShippingCost(
            provider, order, request.distanceKm(), request.withInsurance(), request.withFragile()
        );

        orderService.shipOrder(order.getId(), tracking, cost);
        return ResponseEntity.ok("Kargolandı: " + tracking);
    }
}
