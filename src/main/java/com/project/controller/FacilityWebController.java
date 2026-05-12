package com.project.controller;

import com.project.domain.facility.Facility;
import com.project.service.FacilityService;
import com.project.ui.SessionManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/facilities")
public class FacilityWebController {

    private final FacilityService facilityService;
    private final SessionManager sessionManager;

    public FacilityWebController(FacilityService facilityService, SessionManager sessionManager) {
        this.facilityService = facilityService;
        this.sessionManager = sessionManager;
    }

    @ModelAttribute("currentUser")
    public com.project.domain.user.User currentUser() {
        return sessionManager.getCurrentUser();
    }

    @GetMapping
    public String listFacilities(Model model) {
        List<Facility> facilities = facilityService.listFacilitiesEnsuringDemoData();
        model.addAttribute("facilities", facilities);
        return "facilities";
    }
}
