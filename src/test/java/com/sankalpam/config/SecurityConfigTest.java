package com.sankalpam.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "geosearch.api.key=test-dummy-key-for-unit-tests")
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("API endpoints should be publicly accessible")
    void api_Endpoints_ArePermitted() throws Exception {
        mockMvc.perform(get("/api/v1/sankalpam/cities/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Actuator health should be accessible")
    void actuator_Health_IsPermitted() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS preflight returns OK for allowed origin")
    void cors_Preflight_ReturnsOk() throws Exception {
        mockMvc.perform(options("/api/v1/sankalpam/find")
                        .header("Origin", "https://panchangam-frontend.onrender.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CSRF is disabled for API endpoints")
    void csrf_Disabled() throws Exception {
        // POST without CSRF token should not be blocked
        mockMvc.perform(post("/api/v1/sankalpam/find")
                        .contentType("application/json")
                        .content("{\"date\":\"2026-03-01\",\"time\":\"10:00\",\"city\":\"Sydney\"}"))
                .andExpect(status().isOk());
    }
}

