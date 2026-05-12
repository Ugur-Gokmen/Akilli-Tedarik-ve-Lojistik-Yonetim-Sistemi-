package com.project.controller;

import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.service.UserAdministrationService;
import com.project.ui.SessionManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserWebController {

    private final UserAdministrationService userAdministrationService;
    private final SessionManager sessionManager;

    public UserWebController(UserAdministrationService userAdministrationService, SessionManager sessionManager) {
        this.userAdministrationService = userAdministrationService;
        this.sessionManager = sessionManager;
    }

    @ModelAttribute("currentUser")
    public com.project.domain.user.User currentUser() {
        return sessionManager.getCurrentUser();
    }

    @GetMapping
    public String listUsers(Model model) {
        try {
            List<User> users = userAdministrationService.listAllUsersForAdmin(sessionManager.getCurrentUser());
            model.addAttribute("users", users);
        } catch (SecurityException e) {
            return "redirect:/";
        }
        model.addAttribute("roles", Role.values());
        
        return "users";
    }
}
