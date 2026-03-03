package com.sankalpam.controller;

import com.sankalpam.service.CitySearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "geosearch.api.key=test-dummy-key-for-unit-tests")
class CitySearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CitySearchService citySearchService;

    @Test
    void testSearchCitiesWithValidQuery() throws Exception {
        when(citySearchService.searchCities("San"))
                .thenReturn(Arrays.asList("San Francisco", "San Jose", "San Antonio", "San Diego"));

        mockMvc.perform(get("/api/v1/sankalpam/cities")
                .param("q", "San"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems(
                        "San Francisco",
                        "San Jose",
                        "San Antonio",
                        "San Diego"
                )))
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void testSearchCitiesEmptyQuery() throws Exception {
        when(citySearchService.searchCities(""))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/sankalpam/cities")
                .param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testSearchCitiesNoMatches() throws Exception {
        when(citySearchService.searchCities("XYZ"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/sankalpam/cities")
                .param("q", "XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testSearchCitiesSingleMatch() throws Exception {
        when(citySearchService.searchCities("Pune"))
                .thenReturn(Arrays.asList("Pune", "Pune, India"));

        mockMvc.perform(get("/api/v1/sankalpam/cities")
                .param("q", "Pune"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems("Pune", "Pune, India")))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testSearchCitiesMultipleMatches() throws Exception {
        when(citySearchService.searchCities("New"))
                .thenReturn(Arrays.asList("New York", "New Delhi", "New Haven"));

        mockMvc.perform(get("/api/v1/sankalpam/cities")
                .param("q", "New"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems("New York", "New Delhi", "New Haven")))
                .andExpect(jsonPath("$", hasSize(3)));
    }
}



