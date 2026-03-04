package com.sankalpam.controller;

import com.sankalpam.dto.ApiResponse;
import com.sankalpam.dto.SankalpamFinderRequest;
import com.sankalpam.model.SankalpamFinder;
import com.sankalpam.model.SankalpamPanchangaResponse;
import com.sankalpam.service.SankalpamService;
import com.sankalpam.service.CitySearchService;
import com.sankalpam.service.CityLookupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Sankalpam operations.
 * CORS is handled globally in SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/sankalpam")
public class SankalpamController {

    private final SankalpamService sankalpamService;
    private final CitySearchService citySearchService;
    private final CityLookupService cityLookupService;

    public SankalpamController(SankalpamService sankalpamService,
                               CitySearchService citySearchService,
                               CityLookupService cityLookupService) {
        this.sankalpamService = sankalpamService;
        this.citySearchService = citySearchService;
        this.cityLookupService = cityLookupService;
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
        panchanga.setTimezone(result.getTimezone());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok(
                    "Sankalpam found successfully for the given date, time, and location.",
                    panchanga
                ));
    }

    /**
     * GET /api/v1/sankalpam/cities?q=<searchTerm>
     * Searches for cities using Geoapify Autocomplete API.
     * Returns a list of matching city names.
     */
    @GetMapping({"/cities", "/cities/"})
    public ResponseEntity<List<String>> searchCities(
            @RequestParam(value = "q", defaultValue = "") String searchTerm) {

        List<String> matchingCities = citySearchService.searchCities(searchTerm);

        return ResponseEntity.ok(matchingCities);
    }

    /**
     * GET /api/v1/sankalpam/cities/all
     * Returns all city names currently cached in the local JSON lookup.
     */
    @GetMapping("/cities/all")
    public ResponseEntity<List<String>> getAllCachedCities() {
        List<String> allCities = cityLookupService.getAllCityNames();
        return ResponseEntity.ok(allCities);
    }
}
