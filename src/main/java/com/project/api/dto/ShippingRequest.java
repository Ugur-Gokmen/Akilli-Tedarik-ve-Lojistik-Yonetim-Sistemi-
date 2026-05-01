package com.project.api.dto;

import com.project.infrastructure.factory.CargoProviderFactory.CargoCompany;

/**
 * API katmanı için tasarlanmış Immutable (Değişmez) Veri Taşıma Nesnesi (DTO).
 * Java Record yapısı kullanılarak boilerplate kod (Getter, Constructor, etc.) engellenmiştir.
 */
public record ShippingRequest(
    String orderId,        // İşlem yapılacak siparişin benzersiz kimliği
    CargoCompany company,  // Kullanılacak kargo firması (Enum/Factory)
    String senderCity,     // Gönderici lokasyonu
    String receiverCity,   // Alıcı lokasyonu
    double distanceKm,     // Mesafe bilgisi (Maliyet hesabı için)
    boolean withInsurance, // Sigorta tercihi
    boolean withFragile    // Hassas içerik durumu
) {}