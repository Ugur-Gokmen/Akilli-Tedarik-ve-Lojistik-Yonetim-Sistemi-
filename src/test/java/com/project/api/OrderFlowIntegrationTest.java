package com.project.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.domain.product.SimpleProduct;
import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.repository.ProductRepository;
import com.project.repository.UserRepository;
import com.project.ui.SessionManager;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private SessionManager sessionManager;

    @BeforeEach
    void loginAsAdmin() {
        org.mockito.Mockito.when(sessionManager.isLoggedIn()).thenReturn(true);
        org.mockito.Mockito.when(sessionManager.getCurrentUser()).thenReturn(
            new User("admin", "admin@sirket.com", "x", Role.ADMIN)
        );
    }

    @Test
    void happyPath_create_pay_approve_ship_deliver() throws Exception {
        // product to add into order
        SimpleProduct product = productRepository.save(new SimpleProduct("SSD", "SKU-1", 1000, 1.0, 10, 5));

        // create order
        User customer = userRepository.save(new User("cust", "cust@sirket.com", "x", Role.CUSTOMER));
        String userJson = objectMapper.writeValueAsString(customer);
        MvcResult createRes = mockMvc.perform(post("/api/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isOk())
            .andReturn();

        Map<?, ?> created = objectMapper.readValue(createRes.getResponse().getContentAsString(), Map.class);
        String orderId = String.valueOf(created.get("id"));

        // add item
        mockMvc.perform(post("/api/orders/" + orderId + "/items")
                .param("productId", product.getId())
                .param("qty", "1"))
            .andExpect(status().isOk());

        // pay
        Map<String, Object> paymentReq = Map.of(
            "orderId", orderId,
            "strategyBeanName", "creditCardStrategy",
            "details", Map.of("cardNumber", "4111111111111111")
        );
        mockMvc.perform(post("/api/orders/" + orderId + "/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentReq)))
            .andExpect(status().isOk());

        // approve
        mockMvc.perform(put("/api/orders/" + orderId + "/approve"))
            .andExpect(status().isOk());

        // prepare
        mockMvc.perform(post("/api/orders/" + orderId + "/prepare"))
            .andExpect(status().isOk());

        // ship
        Map<String, Object> shipReq = Map.of(
            "orderId", orderId,
            "company", "ARAS",
            "senderCity", "ISTANBUL",
            "receiverCity", "ANKARA",
            "distanceKm", 450.0,
            "withInsurance", false,
            "withFragile", false
        );
        mockMvc.perform(post("/api/orders/" + orderId + "/ship")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shipReq)))
            .andExpect(status().isOk());

        // deliver
        mockMvc.perform(put("/api/orders/" + orderId + "/deliver"))
            .andExpect(status().isOk());
    }
}

