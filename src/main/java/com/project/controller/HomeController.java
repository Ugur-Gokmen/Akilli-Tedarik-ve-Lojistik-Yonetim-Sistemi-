package com.project.controller;

import com.project.ui.SessionManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Uygulamanın kök dizinini ve genel sayfalarını yönetir.
 */
@Controller
public class HomeController {

    private final SessionManager sessionManager;

    public HomeController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Whitelabel 404 hatasını çözen ana yönlendirme metodu.
     */
    @GetMapping("/")
    public String root() {
        if (sessionManager.isLoggedIn()) {
            return "redirect:/inventory/list"; 
        }
        return "redirect:/auth/login";
    }
}
