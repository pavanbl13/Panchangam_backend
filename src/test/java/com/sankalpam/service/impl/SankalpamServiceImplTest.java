package com.sankalpam.service.impl;

import com.sankalpam.dto.SankalpamFinderRequest;
import com.sankalpam.dto.SankalpamRequest;
import com.sankalpam.model.CityGeoInfo;
import com.sankalpam.model.Coordinates;
import com.sankalpam.model.Sankalpam;
import com.sankalpam.model.SankalpamFinder;
import com.sankalpam.service.GeoLocationService;
import com.sankalpam.service.SankalpamApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SankalpamServiceImpl Tests")
@ExtendWith(MockitoExtension.class)
class SankalpamServiceImplTest {

    @Mock
    private GeoLocationService geoService;

    @Mock
    private SankalpamApiClient apiClient;

    @InjectMocks
    private SankalpamServiceImpl service;

    // ── findSankalpam Tests ──

    @Test
    @DisplayName("findSankalpam should call geoService and apiClient")
    void findSankalpam_ValidRequest_ReturnsFinder() {
        SankalpamFinderRequest req = new SankalpamFinderRequest("2026-03-01", "10:00", "Sydney");

        CityGeoInfo geoInfo = new CityGeoInfo(-33.8688, 151.2093, "Australia/Sydney", "au");
        when(geoService.getGeoInfo("Sydney")).thenReturn(geoInfo);

        SankalpamFinder expected = new SankalpamFinder();
        expected.setSamvatsaram("viSvAvasu");
        expected.setAyanam("uttarAyaNE");
        when(apiClient.fetchSankalpam(eq("Sydney"), any(Coordinates.class),
                eq("Australia/Sydney"), eq("2026-03-01"), eq("10:00")))
                .thenReturn(expected);

        SankalpamFinder result = service.findSankalpam(req);

        assertNotNull(result);
        assertEquals("viSvAvasu", result.getSamvatsaram());
        assertEquals("Australia/Sydney", result.getTimezone());
        verify(geoService).getGeoInfo("Sydney");
        verify(apiClient).fetchSankalpam(eq("Sydney"), any(), eq("Australia/Sydney"), eq("2026-03-01"), eq("10:00"));
    }

    @Test
    @DisplayName("findSankalpam should trim city name")
    void findSankalpam_CityWithSpaces_TrimmedBeforeLookup() {
        SankalpamFinderRequest req = new SankalpamFinderRequest("2026-03-01", "10:00", "  Mumbai  ");

        CityGeoInfo geoInfo = new CityGeoInfo(19.0760, 72.8777, "Asia/Kolkata");
        when(geoService.getGeoInfo("Mumbai")).thenReturn(geoInfo);

        SankalpamFinder expected = new SankalpamFinder();
        when(apiClient.fetchSankalpam(eq("Mumbai"), any(), eq("Asia/Kolkata"), any(), any()))
                .thenReturn(expected);

        SankalpamFinder result = service.findSankalpam(req);
        assertNotNull(result);
        verify(geoService).getGeoInfo("Mumbai");
    }

    // ── submit Tests ──

    @Test
    @DisplayName("submit should return Sankalpam with sanitized fields")
    void submit_ValidRequest_ReturnsSankalpam() {
        SankalpamRequest req = new SankalpamRequest();
        req.setFullName("  Test   User  ");
        req.setGotram("  Bharadwaj  ");
        req.setNakshatram("Rohini");
        req.setRasi("Vrishabha");
        req.setSamvatsaram("Pingala");
        req.setAyanam("Uttarayanam");
        req.setRuthu("Vasantha");
        req.setMasam("Chaitra");
        req.setPaksham("Shukla");
        req.setTithi("Prathama");
        req.setVaasaram("Bhanu");
        req.setCountry("India");
        req.setCity("  Hyderabad  ");
        req.setState("  Telangana  ");
        req.setSankalpaPurpose("  Test purpose  ");
        req.setAdditionalNotes("  Some notes  ");
        req.setEmail("test@test.com");
        req.setPhone("1234567890");

        Sankalpam result = service.submit(req);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test User", result.getFullName());
        assertEquals("Bharadwaj", result.getGotram());
        assertEquals("Hyderabad", result.getCity());
        assertEquals("Telangana", result.getState());
        assertEquals("Test purpose", result.getSankalpaPurpose());
        assertEquals("Some notes", result.getAdditionalNotes());
    }

    @Test
    @DisplayName("submit should handle null additional notes")
    void submit_NullAdditionalNotes_HandlesGracefully() {
        SankalpamRequest req = new SankalpamRequest();
        req.setFullName("Test");
        req.setGotram("Gotram");
        req.setNakshatram("Rohini");
        req.setRasi("Vrishabha");
        req.setSamvatsaram("Pingala");
        req.setAyanam("Uttarayanam");
        req.setRuthu("Vasantha");
        req.setMasam("Chaitra");
        req.setPaksham("Shukla");
        req.setTithi("Prathama");
        req.setVaasaram("Bhanu");
        req.setCountry("India");
        req.setCity("Pune");
        req.setState("Maharashtra");
        req.setSankalpaPurpose("purpose");
        req.setAdditionalNotes(null);
        req.setEmail("t@t.com");
        req.setPhone("123");

        Sankalpam result = service.submit(req);
        assertNull(result.getAdditionalNotes());
    }
}

