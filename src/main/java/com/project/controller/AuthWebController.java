package com.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.ui.Model;
import com.project.repository.UserRepository;
import com.project.ui.SessionManager;



@Controller
@RequestMapping("/auth")
public class AuthWebController {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthWebController(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
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
        return userRepository.findByUsername(username)
            .map(user -> {
                // Basitlik adına doğrudan karşılaştırma yapıyoruz. 
                // (Profesyonel sistemlerde burada BCrypt gibi hash kontrolü olur)
                if (user.getPasswordHash().equals(password)) { 
                    sessionManager.login(user);
                    return "redirect:/inventory/list";
                } else {
                    model.addAttribute("error", "Hatalı şifre!");
                    return "login";
                }
            })
            .orElseGet(() -> {
                model.addAttribute("error", "Kullanıcı bulunamadı!");
                return "login";
            });
    }

    @GetMapping("/logout")
    public String logout() {
        sessionManager.logout();
        return "redirect:/auth/login";
    }
}
