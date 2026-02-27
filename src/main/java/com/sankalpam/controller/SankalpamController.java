package com.sankalpam.controller;

import com.sankalpam.dto.ApiResponse;
import com.sankalpam.dto.SankalpamRequest;
import com.sankalpam.dto.SankalpamFinderRequest;
import com.sankalpam.model.Sankalpam;
import com.sankalpam.model.SankalpamFinder;
import com.sankalpam.model.SankalpamPanchangaResponse;
import com.sankalpam.service.SankalpamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Sankalpam operations.
 * CORS is handled globally in SecurityConfig; @CrossOrigin here is a safety fallback.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sankalpam")
@CrossOrigin(
    origins  = {"http://localhost:5173", "http://localhost:4173"},
    methods  = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
    allowedHeaders = "*"
)
@RequiredArgsConstructor
public class SankalpamController {

    private final SankalpamService sankalpamService;


    /**
     * GET /api/v1/sankalpam/metadata
     * Returns dropdown options for the Panchanga calendar fields.
     * NOTE: Uses HashMap instead of Map.of() to allow >10 entries reliably.
     */
    @GetMapping("/metadata")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("samvatsarams", SankalpamData.SAMVATSARAMS);
        metadata.put("ayanams", SankalpamData.AYANAMS);
        metadata.put("ruthus", SankalpamData.RUTHUS);
        metadata.put("masams", SankalpamData.MASAMS);
        metadata.put("pakshams", SankalpamData.PAKSHAMS);
        metadata.put("tithis", SankalpamData.TITHIS);
        metadata.put("vaasarams", SankalpamData.VAASARAMS);
        metadata.put("nakshatrams", SankalpamData.NAKSHATRAMS);
        metadata.put("rasis", SankalpamData.RASIS);

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(true,"success", metadata, null);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/sankalpam/submit
     * Accepts and validates a Sankalpam form submission.
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Map<String, String>>> submit(
            @Valid @RequestBody SankalpamRequest request) {

        Sankalpam saved = sankalpamService.submit(request);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("referenceId",  saved.getId());
        responseData.put("submittedFor", saved.getFullName());
        responseData.put("submittedAt",  saved.getSubmittedAt().toString());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                    "Sankalpam submitted successfully. May this sankalpa be fulfilled.",
                    responseData
                ));
    }

    /**
     * POST /api/v1/sankalpam/find
     * NEW: Accepts and validates a Sankalpam Finder request (simplified form with date, time, city).
     * Returns calculated Panchanga information for the given date, time, and location.
     */
    @PostMapping("/find")
    public ResponseEntity<ApiResponse<SankalpamPanchangaResponse>> find(
            @Valid @RequestBody SankalpamFinderRequest request) {

        SankalpamFinder result = sankalpamService.findSankalpam(request);

        SankalpamPanchangaResponse panchanga = new SankalpamPanchangaResponse();
        panchanga.setSamvatsaram(result.getSamvatsaram());
        panchanga.setAyanam(result.getAyanam());
        panchanga.setRuthuvu(result.getRuthu());
        panchanga.setMaasam(result.getMasam());
        panchanga.setPaksham(result.getPaksham());
        panchanga.setTithi(result.getTithi());
        panchanga.setVaaram(result.getVaasaram());
        panchanga.setNakshatram(result.getNakshatram());
        panchanga.setSunrise(result.getSunrise());
        panchanga.setSunset(result.getSunset());
        panchanga.setValidUntil(result.getValidUntil());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok(
                    "Sankalpam found successfully for the given date, time, and location.",
                    panchanga
                ));
    }
}
