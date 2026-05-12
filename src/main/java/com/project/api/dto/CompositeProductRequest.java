package com.project.api.dto;

import java.util.List;

public record CompositeProductRequest(
    String name,
    double assemblyFee,
    int initialStock,
    int stockThreshold,
    List<String> componentIds // Sadece alt bileşenlerin ID'lerini alıyoruz!
) {}
