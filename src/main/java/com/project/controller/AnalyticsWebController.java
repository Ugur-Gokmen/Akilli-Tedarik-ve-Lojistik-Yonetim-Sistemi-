package com.project.controller;

import com.project.domain.user.User;
import com.project.service.AnalyticsService;
import com.project.service.AnalyticsService.AnalyticsSnapshot;
import com.project.ui.SessionManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Raporlar ve Analitik sayfası için controller.
 * Stok dağılımı, sipariş istatistikleri ve kritik uyarıları model'e ekler.
 */
@Controller
@RequestMapping("/analytics")
public class AnalyticsWebController {

    private final AnalyticsService analyticsService;
    private final SessionManager sessionManager;

    public AnalyticsWebController(AnalyticsService analyticsService,
                                  SessionManager sessionManager) {
        this.analyticsService = analyticsService;
        this.sessionManager = sessionManager;
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        return sessionManager.getCurrentUser();
    }

    @GetMapping
    public String analyticsPage(Model model) {
        AnalyticsSnapshot snapshot = analyticsService.buildSnapshot();

        model.addAttribute("products", snapshot.products());
        model.addAttribute("orders", snapshot.orders());
        model.addAttribute("simpleCount", snapshot.simpleCount());
        model.addAttribute("compositeCount", snapshot.compositeCount());
        model.addAttribute("simplePercent", snapshot.simplePercent());
        model.addAttribute("compositePercent", snapshot.compositePercent());
        model.addAttribute("criticalCount", snapshot.criticalCount());
        model.addAttribute("totalStock", snapshot.totalStock());
        model.addAttribute("totalOrders", snapshot.totalOrders());
        model.addAttribute("pendingOrders", snapshot.pendingOrders());
        model.addAttribute("shippedOrders", snapshot.shippedOrders());
        model.addAttribute("deliveredOrders", snapshot.deliveredOrders());
        model.addAttribute("cancelledOrders", snapshot.cancelledOrders());
        model.addAttribute("totalRevenue", snapshot.totalRevenue());
        model.addAttribute("lowStockProducts", snapshot.lowStockProducts());

        return "analytics";
    }
}
