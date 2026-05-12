package com.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.domain.order.Order;

@Service
public class OrderManagementService {

    private static final String STATE_PENDING = "BEKLEMEDE";
    private static final String STATE_APPROVED = "ONAYLANDI";
    private static final String STATE_PREPARING = "HAZIRLANIYOR";
    private static final String STATE_SHIPPED = "KARGODA";
    private static final String STATE_DELIVERED = "TESLİM EDİLDİ";

    private final OrderService orderService;

    public OrderManagementService(OrderService orderService) {
        this.orderService = orderService;
    }

    public OrderManagementSnapshot buildSnapshot() {
        List<Order> orders = orderService.listAllOrders();
        return new OrderManagementSnapshot(
            orders,
            countByState(orders, STATE_PENDING),
            countByState(orders, STATE_APPROVED),
            countByState(orders, STATE_PREPARING),
            countByState(orders, STATE_SHIPPED),
            countByState(orders, STATE_DELIVERED));
    }

    private long countByState(List<Order> orders, String stateName) {
        return orders.stream().filter(o -> stateName.equals(o.getCurrentStateName())).count();
    }

    public record OrderManagementSnapshot(
        List<Order> orders,
        long pendingCount,
        long approvedCount,
        long preparingCount,
        long shippedCount,
        long deliveredCount
    ) {}
}
