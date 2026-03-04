package com.sankalpam.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "geosearch.api.key=test-dummy-key-for-unit-tests")
@DisplayName("SpaWebConfig Tests")
class SpaWebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("SPA fallback returns index.html for unknown path")
    void unknownPath_ReturnsIndexHtml() throws Exception {
        mockMvc.perform(get("/some/unknown/spa/route"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Real static asset path resolves correctly")
    void staticAsset_Resolves() throws Exception {
        // index.html is a known static asset
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("API path is not intercepted by SPA handler")
    void apiPath_NotIntercepted() throws Exception {
        mockMvc.perform(get("/api/v1/sankalpam/cities/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Actuator path is not intercepted by SPA handler")
    void actuatorPath_NotIntercepted() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
