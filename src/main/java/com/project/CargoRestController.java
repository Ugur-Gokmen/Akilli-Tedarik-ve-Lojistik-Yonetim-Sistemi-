package com.project.api;

import com.project.api.dto.ShippingRequest;
import com.project.domain.cargo.CargoProvider;
import com.project.domain.order.Order;
import com.project.infrastructure.factory.CargoProviderFactory;
import com.project.service.OrderService;
import com.project.service.Services.CargoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipping")
public class CargoRestController {

    private final CargoService cargoService;
    private final OrderService orderService;

    public CargoRestController(CargoService cargoService, OrderService orderService) {
        this.cargoService = cargoService;
        this.orderService = orderService;
    }

    /**
     * Siparişi kargoya verir ve takip numarası üretir.
     * Burada Adapter Pattern (Factory üzerinden) devreye girer.
     */
    @PostMapping("/ship")
    public ResponseEntity<String> shipOrder(@RequestBody ShippingRequest request) {
        Order order = orderService.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı."));

        // Factory ile doğru Adapter'ı oluştur
        CargoProvider provider = CargoProviderFactory.create(request.company());

        // 1. Kargo ücretini hesapla (Decorator Pattern zinciri burada çalışır)
        double cost = cargoService.calculateShippingCost(
            provider, order, request.distanceKm(), request.withInsurance(), request.withFragile()
        );

        // 2. Takip numarası üret (Adapter API çağrısı)
        String trackingNo = cargoService.generateAndAssignTracking(
            provider, order, request.senderCity(), request.receiverCity()
        );

        // 3. Sipariş durumunu State Pattern ile "KARGODA"ya çek
        orderService.shipOrder(order.getId(), trackingNo, cost);

        return ResponseEntity.ok(String.format(
            "Sipariş başarıyla kargolandı! Firma: %s | Takip No: %s | Ücret: %.2f TL",
            provider.getCompanyName(), trackingNo, cost));
    }
}
