package com.project.api.dto;

import com.project.infrastructure.factory.CargoProviderFactory.CargoCompany;

/**
 * Kargo gönderim isteği DTO - immutable record.
 *
 * <p>Java Record kullanımı sayesinde getter/setter/constructor/equals/hashCode
 * otomatik üretilir. Erişim: {@code request.orderId()}, {@code request.withInsurance()} vb.</p>
 *
 * @param orderId      Kargoya verilecek sipariş ID'si
 * @param company      Kargo şirketi (enum)
 * @param senderCity   Gönderici şehri
 * @param receiverCity Alıcı şehri
 * @param distanceKm   Mesafe (km)
 * @param withInsurance Sigortalı gönderim
 * @param withFragile  Kırılgan eşya koruması
 */
public record ShippingRequest(
    String orderId,
    CargoCompany company,
    String senderCity,
    String receiverCity,
    double distanceKm,
    boolean withInsurance,
    boolean withFragile
) {}