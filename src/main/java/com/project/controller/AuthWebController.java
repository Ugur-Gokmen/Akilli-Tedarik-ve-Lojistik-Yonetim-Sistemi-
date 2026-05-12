package com.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ui.Model;
import com.project.domain.user.Role;
import com.project.ui.SessionManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Controller
@RequestMapping("/auth")
public class AuthWebController {
    private static final Logger log = LoggerFactory.getLogger(AuthWebController.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(5);
    private static final Map<String, LoginAttempt> LOGIN_ATTEMPTS = new ConcurrentHashMap<>();

    private final com.project.service.AuthService authService;
    private final SessionManager sessionManager;

    public AuthWebController(com.project.service.AuthService authService, SessionManager sessionManager) {
        this.authService = authService;
        this.sessionManager = sessionManager;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // templates/login.html'e gider
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, 
                               @RequestParam String password, 
                               Model model) {
        String normalizedUsername = username == null ? "" : username.trim();
        model.addAttribute("username", normalizedUsername);
        boolean hasValidationError = false;

        if (normalizedUsername.isBlank()) {
            model.addAttribute("usernameError", "Kullanıcı adı zorunludur.");
            hasValidationError = true;
        }
        if (password == null || password.isBlank()) {
            model.addAttribute("passwordError", "Şifre zorunludur.");
            hasValidationError = true;
        }
        if (hasValidationError) {
            return "login";
        }

        LoginAttempt attempt = LOGIN_ATTEMPTS.computeIfAbsent(normalizedUsername.toLowerCase(), k -> new LoginAttempt());
        if (attempt.isLocked()) {
            model.addAttribute("lockoutMessage", "Çok fazla hatalı deneme yapıldı. Lütfen 5 dakika sonra tekrar deneyin.");
            return "login";
        }

        try {
            com.project.domain.user.User user = authService.authenticate(normalizedUsername, password);
            sessionManager.login(user);
            LOGIN_ATTEMPTS.remove(normalizedUsername.toLowerCase());
            return "redirect:" + resolveLandingPage(user.getRole());
        } catch (SecurityException e) {
            attempt.registerFailure();
            if (attempt.isLocked()) {
                model.addAttribute("lockoutMessage", "Çok fazla hatalı deneme yapıldı. Lütfen 5 dakika sonra tekrar deneyin.");
            }
            model.addAttribute("error", "Kullanıcı adı veya şifre hatalı!");
            return "login";
        } catch (Exception e) {
            log.error("Web login sırasında beklenmeyen hata. username={}", normalizedUsername, e);
            model.addAttribute("error", "Giriş işlemi sırasında beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        sessionManager.logout();
        return "redirect:/auth/login";
    }

    private String resolveLandingPage(Role role) {
        if (role == Role.CUSTOMER) {
            return "/catalog";
        }
        return "/inventory/list";
    }

    private static final class LoginAttempt {
        private int failedCount;
        private Instant lockoutUntil;

        void registerFailure() {
            failedCount++;
            if (failedCount >= MAX_FAILED_ATTEMPTS) {
                lockoutUntil = Instant.now().plus(LOCKOUT_DURATION);
                failedCount = 0;
            }
        }

        boolean isLocked() {
            return lockoutUntil != null && Instant.now().isBefore(lockoutUntil);
        }
    }
}
