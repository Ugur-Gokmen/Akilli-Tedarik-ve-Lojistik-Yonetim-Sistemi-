package com.project.controller;

import com.project.domain.user.User;
import com.project.service.ProfileApplicationService;
import com.project.ui.SessionManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileWebController {

    private final ProfileApplicationService profileApplicationService;
    private final SessionManager sessionManager;

    public ProfileWebController(ProfileApplicationService profileApplicationService, SessionManager sessionManager) {
        this.profileApplicationService = profileApplicationService;
        this.sessionManager = sessionManager;
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        return sessionManager.getCurrentUser();
    }

    @GetMapping
    public String showProfile(Model model) {
        User user = sessionManager.getCurrentUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/settings")
    public String updateProfile(User profileData, RedirectAttributes redirectAttributes) {
        User user = sessionManager.getCurrentUser();
        if (user != null) {
            profileApplicationService.updateProfile(user, profileData);
            redirectAttributes.addFlashAttribute("successMessage", "Profil bilgileriniz başarıyla güncellendi.");
        }
        return "redirect:/profile";
    }
}
