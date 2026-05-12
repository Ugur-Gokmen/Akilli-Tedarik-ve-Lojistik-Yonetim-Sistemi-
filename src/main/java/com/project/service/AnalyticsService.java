package com.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.domain.order.Order;
import com.project.domain.product.CompositeProduct;
import com.project.domain.product.Product;
import com.project.domain.product.SimpleProduct;

@Service
public class AnalyticsService {

    private static final String STATE_PENDING = "BEKLEMEDE";
    private static final String STATE_SHIPPED = "KARGODA";
    private static final String STATE_DELIVERED = "TESLİM EDİLDİ";
    private static final String STATE_CANCELLED = "İPTAL EDİLDİ";

    private final InventoryService inventoryService;
    private final OrderService orderService;

    public AnalyticsService(InventoryService inventoryService, OrderService orderService) {
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    public AnalyticsSnapshot buildSnapshot() {
        List<Product> products = inventoryService.listAllProducts();
        List<Order> orders = orderService.listAllOrders();
        List<Product> lowStockProducts = inventoryService.getLowStockProducts();

        long simpleCount = products.stream().filter(p -> p instanceof SimpleProduct).count();
        long compositeCount = products.stream().filter(p -> p instanceof CompositeProduct).count();
        long criticalCount = products.stream().filter(p -> p.getStock() <= p.getStockThreshold()).count();
        int totalStock = products.stream().mapToInt(Product::getStock).sum();

        long totalOrders = orders.size();
        long pendingOrders = countByState(orders, STATE_PENDING);
        long shippedOrders = countByState(orders, STATE_SHIPPED);
        long deliveredOrders = countByState(orders, STATE_DELIVERED);
        long cancelledOrders = countByState(orders, STATE_CANCELLED);

        double totalRevenue = orders.stream()
            .filter(Order::isPaid)
            .mapToDouble(Order::getGrandTotal)
            .sum();

        int simplePercent = products.isEmpty() ? 0 : (int) Math.round((simpleCount * 100.0) / products.size());
        int compositePercent = 100 - simplePercent;

        return new AnalyticsSnapshot(
            products,
            orders,
            lowStockProducts,
            simpleCount,
            compositeCount,
            simplePercent,
            compositePercent,
            criticalCount,
            totalStock,
            totalOrders,
            pendingOrders,
            shippedOrders,
            deliveredOrders,
            cancelledOrders,
            totalRevenue);
    }

    private long countByState(List<Order> orders, String stateName) {
        return orders.stream().filter(o -> stateName.equals(o.getCurrentStateName())).count();
    }

    public record AnalyticsSnapshot(
        List<Product> products,
        List<Order> orders,
        List<Product> lowStockProducts,
        long simpleCount,
        long compositeCount,
        int simplePercent,
        int compositePercent,
        long criticalCount,
        int totalStock,
        long totalOrders,
        long pendingOrders,
        long shippedOrders,
        long deliveredOrders,
        long cancelledOrders,
        double totalRevenue
    ) {}
}
