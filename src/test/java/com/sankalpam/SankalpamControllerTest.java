package com.sankalpam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankalpam.dto.SankalpamFinderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "geosearch.api.key=test-dummy-key-for-unit-tests")
class SankalpamControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void find_BlankDate_Returns422() throws Exception {
        SankalpamFinderRequest req = new SankalpamFinderRequest("", "10:00", "Hyderabad");

        mockMvc.perform(post("/api/v1/sankalpam/find")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void find_BlankCity_Returns422() throws Exception {
        SankalpamFinderRequest req = new SankalpamFinderRequest("2026-03-01", "10:00", "");

        mockMvc.perform(post("/api/v1/sankalpam/find")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getAllCachedCities_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/sankalpam/cities/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
