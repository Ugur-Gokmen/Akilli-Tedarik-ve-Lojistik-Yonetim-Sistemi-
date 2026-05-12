package com.project.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.domain.user.User;
import com.project.repository.UserRepository;
import com.project.ui.SessionManager;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final com.project.service.AuthService authService;
    private final SessionManager sessionManager;

    public AuthRestController(com.project.service.AuthService authService, SessionManager sessionManager) {
        this.authService = authService;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password) {
        try {
            User user = authService.authenticate(username, password);
            sessionManager.login(user);
            return ResponseEntity.ok("Giriş başarılı. Aktif Rol: " + user.getRole());
        } catch (SecurityException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body("Kullanıcı adı veya şifre hatalı.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        sessionManager.logout();
        return ResponseEntity.ok("Çıkış yapıldı.");
    }
}
