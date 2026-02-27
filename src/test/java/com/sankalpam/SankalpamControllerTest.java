package com.sankalpam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankalpam.dto.SankalpamRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SankalpamControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private SankalpamRequest validRequest() {
        SankalpamRequest req = new SankalpamRequest();
        req.setFullName("Rama Sharma");
        req.setGotram("Bharadwaja");
        req.setNakshatram("Rohini");
        req.setRasi("Vrishabha (Taurus)");
        req.setSamvatsaram("Pingala");
        req.setAyanam("Uttarayanam");
        req.setRuthu("Vasantha Ruthu");
        req.setMasam("Chaitra");
        req.setPaksham("Shukla Paksham");
        req.setTithi("Prathama");
        req.setVaasaram("Bhanu Vaasaram (Sunday)");
        req.setCountry("USA");
        req.setCity("New York");
        req.setState("New York");
        req.setSankalpaPurpose("Satyanarayana Pooja");
        return req;
    }

    @Test
    void submitSankalpam_ValidRequest_Returns201() throws Exception {
        mockMvc.perform(post("/api/v1/sankalpam/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.referenceId").isNotEmpty());
    }

    @Test
    void submitSankalpam_BlankName_Returns422() throws Exception {
        SankalpamRequest req = validRequest();
        req.setFullName("");

        mockMvc.perform(post("/api/v1/sankalpam/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.fullName").isArray());
    }

    @Test
    void getMetadata_Returns200WithDropdownData() throws Exception {
        mockMvc.perform(get("/api/v1/sankalpam/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.samvatsarams").isArray())
                .andExpect(jsonPath("$.data.masams").isArray())
                .andExpect(jsonPath("$.data.nakshatrams").isArray());
    }
}
