package com.project.domain.order;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStateConverter implements AttributeConverter<OrderState, String> {

    @Override
    public String convertToDatabaseColumn(OrderState attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getStateName();
    }

    @Override
    public OrderState convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        switch (dbData) {
            case "BEKLEMEDE":
            case "PendingState": // Geriye dönük uyumluluk
                return new OrderStates.PendingState();
            case "ONAYLANDI":
            case "ApprovedState":
                return new OrderStates.ApprovedState();
            case "HAZIRLANIYOR":
            case "PreparingState":
                return new OrderStates.PreparingState();
            case "KARGODA":
            case "ShippedState":
                return new OrderStates.ShippedState();
            case "TESLİM EDİLDİ":
            case "DeliveredState":
                return new OrderStates.DeliveredState();
            case "İADE":
            case "ReturnedState":
                return new OrderStates.ReturnedState();
            case "İPTAL EDİLDİ":
            case "CancelledState":
                return new OrderStates.CancelledState();
            default:
                throw new IllegalArgumentException("Bilinmeyen sipariş durumu: " + dbData);
        }
    }
}
