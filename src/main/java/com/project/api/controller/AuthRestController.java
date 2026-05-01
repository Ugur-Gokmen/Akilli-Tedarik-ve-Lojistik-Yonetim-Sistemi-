package com.project.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.domain.user.User;
import com.project.repository.UserRepository;
import com.project.ui.SessionManager;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthRestController(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı!"));
        
        sessionManager.login(user); // Mevcut yapını koruyoruz
        return ResponseEntity.ok("Giriş başarılı. Aktif Rol: " + user.getRole());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        sessionManager.logout();
        return ResponseEntity.ok("Çıkış yapıldı.");
    }
}
