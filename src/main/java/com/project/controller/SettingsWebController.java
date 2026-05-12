package com.project.controller;

import com.project.ui.SessionManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsWebController {

    private final SessionManager sessionManager;

    public SettingsWebController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @ModelAttribute("currentUser")
    public com.project.domain.user.User currentUser() {
        return sessionManager.getCurrentUser();
    }

    @GetMapping
    public String showSettings() {
        return "settings";
    }

    @PostMapping("/shipping")
    public String updateShippingSettings(RedirectAttributes redirectAttributes) {
        // Mock save operation
        redirectAttributes.addFlashAttribute("successMessage", "Kargo stratejileri başarıyla güncellendi.");
        return "redirect:/settings";
    }
}
