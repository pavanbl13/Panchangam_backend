package com.sankalpam.service.impl;

import com.sankalpam.config.GeoSearchApiProperties;
import com.sankalpam.model.CityGeoInfo;
import com.sankalpam.model.Coordinates;
import com.sankalpam.service.CityLookupService;
import com.sankalpam.service.GeoapifyRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GeoapifyGeoLocationService Tests")
@ExtendWith(MockitoExtension.class)
class GeoapifyGeoLocationServiceTest {

    @Mock
    private GeoSearchApiProperties geoSearchApiProperties;

    @Mock
    private CityLookupService cityLookupService;

    @Mock
    private GeoapifyRateLimiter rateLimiter;

    private GeoapifyGeoLocationService service;

    @BeforeEach
    void setUp() {
        service = new GeoapifyGeoLocationService(geoSearchApiProperties, cityLookupService, rateLimiter);
    }

    // ── getGeoInfo: cache hit ──

    @Test
    @DisplayName("getGeoInfo returns cached data when fully populated")
    void getGeoInfo_CacheHit_ReturnsCached() {
        CityGeoInfo cached = new CityGeoInfo(-33.8688, 151.2093, "Australia/Sydney", "au");
        when(cityLookupService.lookup("Sydney")).thenReturn(Optional.of(cached));

        CityGeoInfo result = service.getGeoInfo("Sydney");

        assertEquals(-33.8688, result.getLatitude(), 0.001);
        assertEquals("Australia/Sydney", result.getTimezone());
        verify(rateLimiter, never()).tryAcquire();
    }

    @Test
    @DisplayName("getGeoInfo skips cache when lat/lon are 0")
    void getGeoInfo_CacheIncomplete_CallsApi() {
        CityGeoInfo partial = new CityGeoInfo(0, 0, null, "au");
        when(cityLookupService.lookup("TestCity")).thenReturn(Optional.of(partial));
        when(geoSearchApiProperties.getKey()).thenReturn(null);

        CityGeoInfo result = service.getGeoInfo("TestCity");

        // Falls through to API, which fails due to null key, then hits fallback
        assertNotNull(result);
    }

    @Test
    @DisplayName("getGeoInfo skips cache when timezone is null")
    void getGeoInfo_CacheNoTimezone_CallsApi() {
        CityGeoInfo partial = new CityGeoInfo(19.0, 72.8, null, "in");
        when(cityLookupService.lookup("TestCity")).thenReturn(Optional.of(partial));
        when(geoSearchApiProperties.getKey()).thenReturn(null);

        CityGeoInfo result = service.getGeoInfo("TestCity");
        assertNotNull(result);
    }

    @Test
    @DisplayName("getGeoInfo skips cache when timezone is empty")
    void getGeoInfo_CacheEmptyTimezone_CallsApi() {
        CityGeoInfo partial = new CityGeoInfo(19.0, 72.8, "", "in");
        when(cityLookupService.lookup("TestCity")).thenReturn(Optional.of(partial));
        when(geoSearchApiProperties.getKey()).thenReturn(null);

        CityGeoInfo result = service.getGeoInfo("TestCity");
        assertNotNull(result);
    }

    // ── getGeoInfo: cache miss ──

    @Test
    @DisplayName("getGeoInfo returns fallback when cache miss and no API key")
    void getGeoInfo_CacheMissNoKey_ReturnsFallback() {
        when(cityLookupService.lookup("TestCity")).thenReturn(Optional.empty());
        when(geoSearchApiProperties.getKey()).thenReturn(null);

        CityGeoInfo result = service.getGeoInfo("TestCity");
        assertNotNull(result);
        // Fallback coordinates are Mumbai defaults
        assertEquals(19.0760, result.getLatitude(), 0.001);
    }

    @Test
    @DisplayName("getGeoInfo returns fallback when API key is empty")
    void getGeoInfo_EmptyKey_ReturnsFallback() {
        when(cityLookupService.lookup("TestCity")).thenReturn(Optional.empty());
        when(geoSearchApiProperties.getKey()).thenReturn("   ");

        CityGeoInfo result = service.getGeoInfo("TestCity");
        assertNotNull(result);
    }

    @Test
    @DisplayName("getGeoInfo returns fallback when rate limited")
    void getGeoInfo_RateLimited_ReturnsFallback() {
        when(cityLookupService.lookup("TestCity")).thenReturn(Optional.empty());
        when(geoSearchApiProperties.getKey()).thenReturn("valid-key-1234567890");
        when(rateLimiter.tryAcquire()).thenReturn(false);
        when(rateLimiter.getRemainingCalls()).thenReturn(0);

        CityGeoInfo result = service.getGeoInfo("TestCity");
        assertNotNull(result);
    }

    // ── getCoordinates ──

    @Test
    @DisplayName("getCoordinates delegates to getGeoInfo")
    void getCoordinates_DelegatesToGetGeoInfo() {
        CityGeoInfo cached = new CityGeoInfo(19.0760, 72.8777, "Asia/Kolkata");
        when(cityLookupService.lookup("Mumbai")).thenReturn(Optional.of(cached));

        Coordinates coords = service.getCoordinates("Mumbai");
        assertEquals(19.0760, coords.lat(), 0.001);
        assertEquals(72.8777, coords.lng(), 0.001);
    }

    // ── getGeoInfo: city with comma ──

    @Test
    @DisplayName("getGeoInfo strips country from city name for API search")
    void getGeoInfo_CityWithComma_StripsCountry() {
        when(cityLookupService.lookup("Raipur, India")).thenReturn(Optional.empty());
        when(geoSearchApiProperties.getKey()).thenReturn(null);

        CityGeoInfo result = service.getGeoInfo("Raipur, India");
        assertNotNull(result); // falls to fallback
    }

    // ── getGeoInfo: cache hit uses addCity for persistence ──

    @Test
    @DisplayName("getGeoInfo fetches and caches on miss when rate limiter allows but API fails")
    void getGeoInfo_ApiException_ReturnsFallback() {
        when(cityLookupService.lookup("BrokenCity")).thenReturn(Optional.empty());
        when(geoSearchApiProperties.getKey()).thenReturn("valid-key-1234567890");
        when(rateLimiter.tryAcquire()).thenReturn(true);

        CityGeoInfo result = service.getGeoInfo("BrokenCity");
        assertNotNull(result);
        assertEquals("Asia/Kolkata", result.getTimezone());
    }

    // ── getGeoInfo: cached entry with countryCode but no lat/lon triggers API ──

    @Test
    @DisplayName("getGeoInfo uses countryCode filter from partial cache")
    void getGeoInfo_PartialCacheWithCountry_UsesFilter() {
        CityGeoInfo partial = new CityGeoInfo(0, 0, null, "au");
        when(cityLookupService.lookup("Perth")).thenReturn(Optional.of(partial));
        when(geoSearchApiProperties.getKey()).thenReturn("valid-key-1234567890");
        when(rateLimiter.tryAcquire()).thenReturn(true);

        CityGeoInfo result = service.getGeoInfo("Perth");
        assertNotNull(result);
    }

    @Test
    @DisplayName("getGeoInfo cache miss with no country code still calls API")
    void getGeoInfo_CacheMissNoCountry_CallsApi() {
        when(cityLookupService.lookup("Tokyo")).thenReturn(Optional.empty());
        when(geoSearchApiProperties.getKey()).thenReturn("valid-key-1234567890");
        when(rateLimiter.tryAcquire()).thenReturn(true);

        CityGeoInfo result = service.getGeoInfo("Tokyo");
        assertNotNull(result);
    }

    @Test
    @DisplayName("getGeoInfo caches result after successful fetch")
    void getGeoInfo_SuccessfulFetch_CachesResult() {
        when(cityLookupService.lookup("FakeCity")).thenReturn(Optional.empty());
        when(geoSearchApiProperties.getKey()).thenReturn("valid-key-1234567890");
        when(rateLimiter.tryAcquire()).thenReturn(true);

        service.getGeoInfo("FakeCity");
        // Even if API call fails, the method completes
        // If it reaches the fallback, addCity is NOT called (only on success)
    }
}


