package com.sankalpam.service.impl;

import com.sankalpam.dto.SankalpamRequest;
import com.sankalpam.dto.SankalpamFinderRequest;
import com.sankalpam.model.Coordinates;
import com.sankalpam.model.Sankalpam;
import com.sankalpam.model.SankalpamFinder;
import com.sankalpam.service.GeoLocationService;
import com.sankalpam.service.SankalpamApiClient;
import com.sankalpam.service.SankalpamService;
import com.sankalpam.service.TimeZoneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Sankalpam business logic.
 * Implements the SankalpamService interface.
 */
@Slf4j
@Service
public class SankalpamServiceImpl implements SankalpamService {

    private final GeoLocationService geoService;
    private final TimeZoneService tzService;
    private final SankalpamApiClient apiClient;

    public SankalpamServiceImpl(
            GeoLocationService geoService,
            TimeZoneService tzService,
            SankalpamApiClient apiClient
    ) {
        this.geoService = geoService;
        this.tzService = tzService;
        this.apiClient = apiClient;
    }


    @Override
    public Sankalpam submit(SankalpamRequest request) {
        log.info("Processing Sankalpam for: {}", maskName(request.getFullName()));

        Sankalpam sankalpam = Sankalpam.builder()
                .fullName(sanitize(request.getFullName()))
                .gotram(sanitize(request.getGotram()))
                .nakshatram(request.getNakshatram())
                .rasi(request.getRasi())
                .samvatsaram(request.getSamvatsaram())
                .ayanam(request.getAyanam())
                .ruthu(request.getRuthu())
                .masam(request.getMasam())
                .paksham(request.getPaksham())
                .tithi(request.getTithi())
                .vaasaram(request.getVaasaram())
                .country(request.getCountry())
                .city(sanitize(request.getCity()))
                .state(sanitize(request.getState()))
                .sankalpaPurpose(sanitize(request.getSankalpaPurpose()))
                .additionalNotes(request.getAdditionalNotes() != null
                        ? sanitize(request.getAdditionalNotes()) : null)
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        // TODO: persist via sankalpamRepository.save(sankalpam)
        log.info("Sankalpam processed. ID: {}", sankalpam.getId());
        return sankalpam;
    }

    /*public SankalpamFinder findSankalpam(SankalpamFinderRequest request) {
        log.info("Finding Sankalpam for: {}, time: {}, city: {}",
                request.getDate(), request.getTime(), sanitize(request.getCity()));

        SankalpamFinder finder = new SankalpamFinder(
                request.getDate(),
                request.getTime(),
                sanitize(request.getCity())
        );

        // TODO: In production, calculate actual Panchanga values based on date, time, and location
        // For now, returning default/placeholder values
        finder.setSamvatsaram("Pingala");
        finder.setAyanam("Uttarayanam");
        finder.setRuthu("Vasantha Ruthu");
        finder.setMasam("Chaitra");
        finder.setPaksham("Shukla Paksham");
        finder.setTithi("Prathama");
        finder.setVaasaram("Bhanu Vaasaram (Sunday)");
        finder.setNakshatram("Rohini");
        finder.setRasi("Vrishabha (Taurus)");

        // Set sunrise, sunset, and validUntil times
        finder.setSunrise("06:18");
        finder.setSunset("20:45");
        finder.setValidUntil("2026-02-25T18:35");

        log.info("Sankalpam found. ID: {}", finder.getId());
        return finder;
    }*/

    @Override
    public SankalpamFinder findSankalpam(SankalpamFinderRequest request) {
        String city = request.getCity().trim();
        Coordinates coords = geoService.getCoordinates(city);
        String timezone = tzService.getTimeZone(coords);
        return apiClient.fetchSankalpam(city, coords, timezone, request.getDate(), request.getTime());
    }


    private String sanitize(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s+", " ");
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2) return "***";
        return name.charAt(0) + "***";
    }
}
