package com.project.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.project.config.SecurityConfig;
import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.infrastructure.security.SessionAuthenticationFilter;
import com.project.service.InventoryApplicationService;
import com.project.ui.SessionManager;

@WebMvcTest(controllers = InventoryRestController.class)
@Import({ SecurityConfig.class, SessionAuthenticationFilter.class })
@ActiveProfiles("test")
class InventorySecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionManager sessionManager;

    @MockBean
    private InventoryApplicationService inventoryApplicationService;

    @Test
    void api_inventory_requires_authentication() throws Exception {
        org.mockito.Mockito.when(sessionManager.isLoggedIn()).thenReturn(false);

        mockMvc.perform(get("/api/inventory/list"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void api_inventory_returns_forbidden_for_wrong_role() throws Exception {
        org.mockito.Mockito.when(sessionManager.isLoggedIn()).thenReturn(true);
        org.mockito.Mockito.when(sessionManager.getCurrentUser()).thenReturn(
            new User("cust", "cust@sirket.com", "x", Role.CUSTOMER)
        );

        mockMvc.perform(get("/api/inventory/list"))
            .andExpect(status().isForbidden());
    }
}

