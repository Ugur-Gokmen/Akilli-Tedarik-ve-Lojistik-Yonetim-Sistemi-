package com.project.service;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.project.domain.cargo.CargoProvider;
import com.project.domain.order.Order;
import com.project.infrastructure.factory.CargoProviderFactory;
import com.project.infrastructure.resolver.CargoProviderResolver;
import com.project.service.Services.CargoService;

/**
 * Web/REST katmanları için kargo orchestration servisi.
 */
@Service
public class ShippingApplicationService {

    public record ShipmentResult(String trackingNumber, double shippingCost) {}

    private final CargoService cargoService;
    private final OrderService orderService;
    private final CargoProviderResolver cargoProviderResolver;

    public ShippingApplicationService(CargoService cargoService, OrderService orderService, CargoProviderResolver cargoProviderResolver) {
        this.cargoService = Objects.requireNonNull(cargoService, "cargoService");
        this.orderService = Objects.requireNonNull(orderService, "orderService");
        this.cargoProviderResolver = Objects.requireNonNull(cargoProviderResolver, "cargoProviderResolver");
    }

    public ShipmentResult shipOrder(String orderId,
                                    CargoProviderFactory.CargoCompany company,
                                    String senderCity,
                                    String receiverCity,
                                    double distanceKm,
                                    boolean withInsurance,
                                    boolean withFragile) {
        Order order = orderService.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı: " + orderId));

        CargoProvider provider = cargoProviderResolver.resolve(company);

        String trackingNumber = cargoService.generateAndAssignTracking(provider, order, senderCity, receiverCity);
        double shippingCost = cargoService.calculateShippingCost(provider, order, distanceKm, withInsurance, withFragile);

        orderService.shipOrder(orderId, trackingNumber, shippingCost);
        return new ShipmentResult(trackingNumber, shippingCost);
    }
}

