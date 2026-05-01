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
        return attribute.getClass().getSimpleName();
    }

    @Override
    public OrderState convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        switch (dbData) {
            case "PendingState":
                return new OrderStates.PendingState();
            case "ApprovedState":
                return new OrderStates.ApprovedState();
            case "PreparingState":
                return new OrderStates.PreparingState();
            case "ShippedState":
                return new OrderStates.ShippedState();
            case "DeliveredState":
                return new OrderStates.DeliveredState();
            case "ReturnedState":
                return new OrderStates.ReturnedState();
            case "CancelledState":
                return new OrderStates.CancelledState();
            default:
                throw new IllegalArgumentException("Bilinmeyen sipariş durumu: " + dbData);
        }
    }
}
