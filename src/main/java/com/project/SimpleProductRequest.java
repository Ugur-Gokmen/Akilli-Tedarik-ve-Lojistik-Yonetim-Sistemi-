package com.project.api.dto;

public record SimpleProductRequest(
    String name,
    String sku,
    double unitPrice,
    double weightKg,
    int initialStock,
    int stockThreshold
) {}
