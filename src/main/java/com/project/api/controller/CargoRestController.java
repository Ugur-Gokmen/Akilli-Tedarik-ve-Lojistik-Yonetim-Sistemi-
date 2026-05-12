package com.project.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.api.dto.ShippingRequest;
import com.project.service.ShippingApplicationService;

/**
 * REST Controller - Kargo işlemlerini dış dünyaya (API) açan katman.
 * DTO (Record) kullanımı ile veri güvenliği ve immutability sağlanmıştır.
 */
@RestController
@RequestMapping("/api/cargo")
public class CargoRestController {

    private final ShippingApplicationService shippingApplicationService;

    @Autowired
    public CargoRestController(ShippingApplicationService shippingApplicationService) {
        this.shippingApplicationService = shippingApplicationService;
    }

    /**
     * Siparişi kargoya verir ve maliyet hesaplar.
     * Record erişimi: request.withInsurance() (get/is ön eki yoktur).
     */
    @PostMapping("/ship")
    public ResponseEntity<?> shipOrder(@RequestBody ShippingRequest request) {
        var result = shippingApplicationService.shipOrder(
            request.orderId(),
            request.company(),
            request.senderCity(),
            request.receiverCity(),
            request.distanceKm(),
            request.withInsurance(),
            request.withFragile());
        return ResponseEntity.ok("Kargolandı: " + result.trackingNumber());
    }
}
