package com.sankalpam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankalpam.dto.SankalpamFinderRequest;
import com.sankalpam.model.SankalpamFinder;
import com.sankalpam.service.CitySearchService;
import com.sankalpam.service.SankalpamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "geosearch.api.key=test-dummy-key-for-unit-tests")
class UiFlowCorsEmulationTest {

    private static final String UI_ORIGIN = "https://panchangam-frontend.onrender.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CitySearchService citySearchService;

    @MockBean
    private SankalpamService sankalpamService;

    @Test
    void step1_healthEndpoint_AllowsUiOrigin() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .header(HttpHeaders.ORIGIN, UI_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", UI_ORIGIN));
    }

    @Test
    void step2_citiesEndpoint_AllowsUiOrigin_AndReturnsData() throws Exception {
        when(citySearchService.searchCities("sydn")).thenReturn(List.of("Sydney"));

        mockMvc.perform(get("/api/v1/sankalpam/cities")
                        .param("q", "sydn")
                        .header(HttpHeaders.ORIGIN, UI_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", UI_ORIGIN))
                .andExpect(jsonPath("$[0]").value("Sydney"));
    }

    @Test
    void step3_findEndpoint_AllowsUiOrigin_AndReturnsSuccessPayload() throws Exception {
        SankalpamFinder result = new SankalpamFinder();
        result.setSamvatsaram("Krodhi");
        result.setAyanam("Uttarayanam");
        result.setRuthu("Vasantha Ruthu");
        result.setMasam("Chaitra");
        result.setPaksham("Shukla Paksham");
        result.setTithi("Prathama");
        result.setVaasaram("Bhanu Vaasaram (Sunday)");
        result.setNakshatram("Ashwini");
        result.setSunrise("06:10");
        result.setSunset("18:05");
        result.setValidUntil("2026-03-03T06:00:00Z");

        when(sankalpamService.findSankalpam(any(SankalpamFinderRequest.class))).thenReturn(result);

        SankalpamFinderRequest request = new SankalpamFinderRequest("2026-03-02", "09:30", "Sydney");

        mockMvc.perform(post("/api/v1/sankalpam/find")
                        .header(HttpHeaders.ORIGIN, UI_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", UI_ORIGIN))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.samvatsaram").value("Krodhi"));
    }

    @Test
    void preflightForFind_FromUiOrigin_ReturnsCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/sankalpam/find")
                        .header(HttpHeaders.ORIGIN, UI_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", UI_ORIGIN));
    }
}
