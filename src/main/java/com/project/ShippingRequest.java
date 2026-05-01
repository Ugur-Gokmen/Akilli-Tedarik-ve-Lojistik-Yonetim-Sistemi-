package com.project.api.dto;

import com.project.infrastructure.factory.CargoProviderFactory.CargoCompany;

public record ShippingRequest(
    String orderId,
    CargoCompany company,
    String senderCity,
    String receiverCity,
    double distanceKm,
    boolean withInsurance,
    boolean withFragile
) {}
