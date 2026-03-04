package com.sankalpam.service.impl;

import com.sankalpam.config.GeoSearchApiProperties;
import com.sankalpam.model.CityGeoInfo;
import com.sankalpam.service.CityLookupService;
import com.sankalpam.service.GeoapifyRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GeoapifyCitySearchService Tests")
@ExtendWith(MockitoExtension.class)
class GeoapifyCitySearchServiceTest {

    @Mock
    private GeoSearchApiProperties geoSearchApiProperties;

    @Mock
    private GeoapifyRateLimiter rateLimiter;

    @Mock
    private CityLookupService cityLookupService;

    private GeoapifyCitySearchService service;

    @BeforeEach
    void setUp() {
        service = new GeoapifyCitySearchService(geoSearchApiProperties, rateLimiter, cityLookupService);
    }

    // ── Helper: set up mocks so that rate limiter passes and local cache misses ──
    private void setupApiCallPreconditions(String query) {
        when(cityLookupService.searchByPrefix(query)).thenReturn(List.of());
        when(geoSearchApiProperties.getKey()).thenReturn("test-api-key-1234567890");
        when(rateLimiter.tryAcquire()).thenReturn(true);
    }

    @SuppressWarnings("unchecked")
    private HttpClient mockHttpResponse(MockedStatic<HttpClient> httpClientStatic, int statusCode, String body) throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        HttpClient.Builder mockBuilder = mock(HttpClient.Builder.class);

        httpClientStatic.when(HttpClient::newBuilder).thenReturn(mockBuilder);
        when(mockBuilder.connectTimeout(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockClient);

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(statusCode);
        when(mockResponse.body()).thenReturn(body);
        when(mockClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        return mockClient;
    }

    // ══════════════════════════════════════════════════════════
    //  searchCities: null / empty / blank input
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Null/Empty/Blank input")
    class NullEmptyInput {

        @Test
        @DisplayName("searchCities returns empty for null query")
        void searchCities_NullQuery_ReturnsEmpty() {
            assertTrue(service.searchCities(null).isEmpty());
        }

        @Test
        @DisplayName("searchCities returns empty for empty query")
        void searchCities_EmptyQuery_ReturnsEmpty() {
            assertTrue(service.searchCities("").isEmpty());
        }

        @Test
        @DisplayName("searchCities returns empty for blank query")
        void searchCities_BlankQuery_ReturnsEmpty() {
            assertTrue(service.searchCities("   ").isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  searchCities: local cache hit
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Local cache hit")
    class LocalCacheHit {

        @Test
        @DisplayName("searchCities returns local matches when found")
        void searchCities_LocalCacheHit_ReturnsLocalResults() {
            when(cityLookupService.searchByPrefix("Syd")).thenReturn(List.of("Sydney"));

            List<String> result = service.searchCities("Syd");
            assertEquals(1, result.size());
            assertEquals("Sydney", result.get(0));
            verify(rateLimiter, never()).tryAcquire();
        }

        @Test
        @DisplayName("searchCities returns multiple local matches")
        void searchCities_MultipleCacheHits_ReturnsAll() {
            when(cityLookupService.searchByPrefix("New")).thenReturn(List.of("New York", "New Delhi"));

            List<String> result = service.searchCities("New");
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("searchCities with spaces passes raw query to searchByPrefix")
        void searchCities_QueryWithSpaces_Trimmed() {
            when(cityLookupService.searchByPrefix("  San  ")).thenReturn(List.of("San Francisco"));

            List<String> result = service.searchCities("  San  ");
            assertFalse(result.isEmpty());
            assertEquals("San Francisco", result.get(0));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  searchCities: no local match, no API key / empty key
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("No API key")
    class NoApiKey {

        @Test
        @DisplayName("searchCities returns empty when no local match and no API key")
        void searchCities_NoLocalNoKey_ReturnsEmpty() {
            when(cityLookupService.searchByPrefix("xyz")).thenReturn(List.of());
            when(geoSearchApiProperties.getKey()).thenReturn(null);

            assertTrue(service.searchCities("xyz").isEmpty());
        }

        @Test
        @DisplayName("searchCities returns empty when API key is empty")
        void searchCities_EmptyKey_ReturnsEmpty() {
            when(cityLookupService.searchByPrefix("xyz")).thenReturn(List.of());
            when(geoSearchApiProperties.getKey()).thenReturn("  ");

            assertTrue(service.searchCities("xyz").isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  searchCities: rate limited
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("searchCities returns empty when rate limited")
    void searchCities_RateLimited_ReturnsEmpty() {
        when(cityLookupService.searchByPrefix("xyz")).thenReturn(List.of());
        when(geoSearchApiProperties.getKey()).thenReturn("valid-api-key-1234567890");
        when(rateLimiter.tryAcquire()).thenReturn(false);
        when(rateLimiter.getRemainingCalls()).thenReturn(0);

        assertTrue(service.searchCities("xyz").isEmpty());
    }

    // ══════════════════════════════════════════════════════════
    //  searchCities: API call that fails (exception)
    // ══════════════════════════════════════════════════════════

    @Test
    @DisplayName("searchCities handles API exception gracefully")
    void searchCities_ApiException_ReturnsEmpty() {
        when(cityLookupService.searchByPrefix("broken")).thenReturn(List.of());
        when(geoSearchApiProperties.getKey()).thenReturn("valid-api-key-1234567890");
        when(rateLimiter.tryAcquire()).thenReturn(true);

        List<String> result = service.searchCities("broken");
        assertNotNull(result);
    }

    @Test
    @DisplayName("searchCities with API key but exception in HTTP call returns empty")
    void searchCities_HttpCallFails_ReturnsEmpty() {
        when(cityLookupService.searchByPrefix("badcity")).thenReturn(List.of());
        when(geoSearchApiProperties.getKey()).thenReturn("a-valid-key-for-geoapify");
        when(rateLimiter.tryAcquire()).thenReturn(true);
        lenient().when(cityLookupService.lookup(anyString())).thenReturn(Optional.empty());

        List<String> result = service.searchCities("badcity");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════════
    //  searchCities: Mocked HTTP responses (200, non-200)
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Mocked HTTP responses")
    class MockedHttpResponses {

        @Test
        @DisplayName("200 with city results returns parsed city names")
        void searchCities_200WithCities_ReturnsParsedCities() throws Exception {
            setupApiCallPreconditions("London");
            when(cityLookupService.lookup(anyString())).thenReturn(Optional.empty());

            String responseJson = """
                {
                  "results": [
                    {"city": "London", "country_code": "gb", "lat": 51.5074, "lon": -0.1278,
                     "timezone": {"name": "Europe/London"}},
                    {"city": "Londonderry", "country_code": "gb", "lat": 54.9966, "lon": -7.3086,
                     "timezone": {"name": "Europe/London"}}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                List<String> result = service.searchCities("London");

                assertEquals(2, result.size());
                assertTrue(result.contains("London"));
                assertTrue(result.contains("Londonderry"));
            }
        }

        @Test
        @DisplayName("200 with duplicate cities returns unique set")
        void searchCities_200WithDuplicates_ReturnsUnique() throws Exception {
            setupApiCallPreconditions("Paris");
            lenient().when(cityLookupService.lookup(anyString())).thenReturn(Optional.empty());

            String responseJson = """
                {
                  "results": [
                    {"city": "Paris", "country_code": "fr"},
                    {"city": "Paris", "country_code": "us"},
                    {"city": "Paris", "country_code": "ca"}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                List<String> result = service.searchCities("Paris");

                // Duplicates removed by LinkedHashSet
                assertEquals(1, result.size());
                assertEquals("Paris", result.get(0));
            }
        }

        @Test
        @DisplayName("200 with empty results array returns empty")
        void searchCities_200EmptyResults_ReturnsEmpty() throws Exception {
            setupApiCallPreconditions("zzzzz");

            String responseJson = """
                {"results": []}
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                List<String> result = service.searchCities("zzzzz");
                assertTrue(result.isEmpty());
            }
        }

        @Test
        @DisplayName("200 with null results field returns empty")
        void searchCities_200NullResults_ReturnsEmpty() throws Exception {
            setupApiCallPreconditions("zzzzz");

            String responseJson = """
                {"status": "ok"}
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                List<String> result = service.searchCities("zzzzz");
                assertTrue(result.isEmpty());
            }
        }

        @Test
        @DisplayName("Non-200 status returns empty list")
        void searchCities_Non200Status_ReturnsEmpty() throws Exception {
            setupApiCallPreconditions("test");

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 403, "{\"error\":\"forbidden\"}");

                List<String> result = service.searchCities("test");
                assertTrue(result.isEmpty());
            }
        }

        @Test
        @DisplayName("500 status returns empty list")
        void searchCities_500Status_ReturnsEmpty() throws Exception {
            setupApiCallPreconditions("test");

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 500, "Internal Server Error");

                List<String> result = service.searchCities("test");
                assertTrue(result.isEmpty());
            }
        }

        @Test
        @DisplayName("HttpClient.send throws exception returns empty")
        @SuppressWarnings("unchecked")
        void searchCities_SendThrowsException_ReturnsEmpty() throws Exception {
            setupApiCallPreconditions("test");

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                HttpClient mockClient = mock(HttpClient.class);
                HttpClient.Builder mockBuilder = mock(HttpClient.Builder.class);

                httpClientStatic.when(HttpClient::newBuilder).thenReturn(mockBuilder);
                when(mockBuilder.connectTimeout(any())).thenReturn(mockBuilder);
                when(mockBuilder.build()).thenReturn(mockClient);
                when(mockClient.send(any(), any(HttpResponse.BodyHandler.class)))
                        .thenThrow(new java.io.IOException("Connection refused"));

                List<String> result = service.searchCities("test");
                assertTrue(result.isEmpty());
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  extractCityName: various branches
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("extractCityName branches")
    class ExtractCityName {

        @Test
        @DisplayName("Falls back to formatted field when city is null")
        void searchCities_NoCityField_UsesFormatted() throws Exception {
            setupApiCallPreconditions("Berlin");
            lenient().when(cityLookupService.lookup(anyString())).thenReturn(Optional.empty());

            String responseJson = """
                {
                  "results": [
                    {"formatted": "Berlin, Germany", "country_code": "de"}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                List<String> result = service.searchCities("Berlin");
                assertEquals(1, result.size());
                assertEquals("Berlin", result.get(0));   // split on comma, trimmed
            }
        }

        @Test
        @DisplayName("Falls back to formatted field when city is empty string")
        void searchCities_EmptyCityField_UsesFormatted() throws Exception {
            setupApiCallPreconditions("Tokyo");
            lenient().when(cityLookupService.lookup(anyString())).thenReturn(Optional.empty());

            String responseJson = """
                {
                  "results": [
                    {"city": "", "formatted": "Tokyo, Japan", "country_code": "jp"}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                List<String> result = service.searchCities("Tokyo");
                assertEquals(1, result.size());
                assertEquals("Tokyo", result.get(0));
            }
        }

        @Test
        @DisplayName("Returns null for result with no city and no formatted field")
        void searchCities_NoCityNoFormatted_Skipped() throws Exception {
            setupApiCallPreconditions("unknown");

            String responseJson = """
                {
                  "results": [
                    {"lat": 0.0, "lon": 0.0}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                List<String> result = service.searchCities("unknown");
                assertTrue(result.isEmpty());
            }
        }

        @Test
        @DisplayName("Formatted field without comma returns full text")
        void searchCities_FormattedNoComma_ReturnsFullText() throws Exception {
            setupApiCallPreconditions("Rome");
            lenient().when(cityLookupService.lookup(anyString())).thenReturn(Optional.empty());

            String responseJson = """
                {
                  "results": [
                    {"formatted": "Rome", "country_code": "it"}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                List<String> result = service.searchCities("Rome");
                assertEquals(1, result.size());
                assertEquals("Rome", result.get(0));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  cacheCountryCode: various branches
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cacheCountryCode branches")
    class CacheCountryCode {

        @Test
        @DisplayName("Caches country code, lat, lon, timezone for new city")
        void cacheCountryCode_NewCity_CachesAll() throws Exception {
            setupApiCallPreconditions("Sydney");
            when(cityLookupService.lookup("Sydney")).thenReturn(Optional.empty());

            String responseJson = """
                {
                  "results": [
                    {"city": "Sydney", "country_code": "AU", "lat": -33.8688, "lon": 151.2093,
                     "timezone": {"name": "Australia/Sydney"}}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                service.searchCities("Sydney");

                verify(cityLookupService).addCity(eq("Sydney"), argThat(info ->
                        "au".equals(info.getCountryCode()) &&
                        info.getLatitude() == -33.8688 &&
                        info.getLongitude() == 151.2093 &&
                        "Australia/Sydney".equals(info.getTimezone())
                ));
            }
        }

        @Test
        @DisplayName("Skips caching when country_code is missing from result")
        void cacheCountryCode_NoCountryCode_SkipsCache() throws Exception {
            setupApiCallPreconditions("test");

            String responseJson = """
                {
                  "results": [
                    {"city": "TestCity", "lat": 10.0, "lon": 20.0}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                service.searchCities("test");

                verify(cityLookupService, never()).addCity(anyString(), any());
            }
        }

        @Test
        @DisplayName("Skips caching when country_code is null node")
        void cacheCountryCode_NullCountryCodeNode_SkipsCache() throws Exception {
            setupApiCallPreconditions("test");

            String responseJson = """
                {
                  "results": [
                    {"city": "TestCity", "country_code": null}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                service.searchCities("test");

                verify(cityLookupService, never()).addCity(anyString(), any());
            }
        }

        @Test
        @DisplayName("Updates existing cache entry when it has no country code")
        void cacheCountryCode_ExistingNoCountry_Updates() throws Exception {
            setupApiCallPreconditions("Mumbai");
            CityGeoInfo existing = new CityGeoInfo(19.0, 72.8, "Asia/Kolkata");
            // countryCode is null by default
            when(cityLookupService.lookup("Mumbai")).thenReturn(Optional.of(existing));

            String responseJson = """
                {
                  "results": [
                    {"city": "Mumbai", "country_code": "in", "lat": 19.076, "lon": 72.877,
                     "timezone": {"name": "Asia/Kolkata"}}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                service.searchCities("Mumbai");

                verify(cityLookupService).addCity(eq("Mumbai"), argThat(info ->
                        "in".equals(info.getCountryCode())
                ));
            }
        }

        @Test
        @DisplayName("Skips update when existing cache entry already has country code")
        void cacheCountryCode_ExistingHasCountry_SkipsUpdate() throws Exception {
            setupApiCallPreconditions("Tokyo");
            CityGeoInfo existing = new CityGeoInfo(35.6762, 139.6503, "Asia/Tokyo", "jp");
            when(cityLookupService.lookup("Tokyo")).thenReturn(Optional.of(existing));

            String responseJson = """
                {
                  "results": [
                    {"city": "Tokyo", "country_code": "jp", "lat": 35.6762, "lon": 139.6503}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                service.searchCities("Tokyo");

                verify(cityLookupService, never()).addCity(anyString(), any());
            }
        }

        @Test
        @DisplayName("Caches country code without lat/lon/timezone when not in result")
        void cacheCountryCode_NoLatLonTimezone_CachesCountryOnly() throws Exception {
            setupApiCallPreconditions("test");
            when(cityLookupService.lookup("TestCity")).thenReturn(Optional.empty());

            String responseJson = """
                {
                  "results": [
                    {"city": "TestCity", "country_code": "us"}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                service.searchCities("test");

                verify(cityLookupService).addCity(eq("TestCity"), argThat(info ->
                        "us".equals(info.getCountryCode()) &&
                        info.getLatitude() == 0 &&
                        info.getLongitude() == 0
                ));
            }
        }

        @Test
        @DisplayName("Caches lat/lon but no timezone when timezone absent")
        void cacheCountryCode_LatLonNoTimezone_CachesPartial() throws Exception {
            setupApiCallPreconditions("test");
            when(cityLookupService.lookup("Delhi")).thenReturn(Optional.empty());

            String responseJson = """
                {
                  "results": [
                    {"city": "Delhi", "country_code": "in", "lat": 28.6139, "lon": 77.209}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                service.searchCities("test");

                verify(cityLookupService).addCity(eq("Delhi"), argThat(info ->
                        "in".equals(info.getCountryCode()) &&
                        info.getLatitude() == 28.6139
                ));
            }
        }

        @Test
        @DisplayName("Caches timezone when timezone object present but no name field")
        void cacheCountryCode_TimezoneNoName_SkipsTimezone() throws Exception {
            setupApiCallPreconditions("test");
            when(cityLookupService.lookup("Cairo")).thenReturn(Optional.empty());

            String responseJson = """
                {
                  "results": [
                    {"city": "Cairo", "country_code": "eg", "lat": 30.0, "lon": 31.2,
                     "timezone": {"offset": "+02:00"}}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                service.searchCities("test");

                verify(cityLookupService).addCity(eq("Cairo"), argThat(info ->
                        "eg".equals(info.getCountryCode()) &&
                        info.getTimezone() == null
                ));
            }
        }

        @Test
        @DisplayName("Handles multiple results with mixed caching scenarios")
        void cacheCountryCode_MultipleResults_MixedScenarios() throws Exception {
            setupApiCallPreconditions("San");
            when(cityLookupService.lookup("San Francisco")).thenReturn(Optional.empty());
            when(cityLookupService.lookup("San Diego")).thenReturn(
                    Optional.of(new CityGeoInfo(32.7157, -117.1611, "America/Los_Angeles", "us")));

            String responseJson = """
                {
                  "results": [
                    {"city": "San Francisco", "country_code": "us", "lat": 37.7749, "lon": -122.4194,
                     "timezone": {"name": "America/Los_Angeles"}},
                    {"city": "San Diego", "country_code": "us", "lat": 32.7157, "lon": -117.1611}
                  ]
                }
                """;

            try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
                mockHttpResponse(httpClientStatic, 200, responseJson);

                List<String> result = service.searchCities("San");

                assertEquals(2, result.size());
                // San Francisco should be cached (new), San Diego should NOT be updated (already has country code)
                verify(cityLookupService).addCity(eq("San Francisco"), any());
                verify(cityLookupService, never()).addCity(eq("San Diego"), any());
            }
        }
    }
}


