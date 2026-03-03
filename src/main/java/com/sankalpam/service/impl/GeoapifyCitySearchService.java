package com.sankalpam.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankalpam.config.GeoSearchApiProperties;
import com.sankalpam.model.CityGeoInfo;
import com.sankalpam.service.CityLookupService;
import com.sankalpam.service.CitySearchService;
import com.sankalpam.service.GeoapifyRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class GeoapifyCitySearchService implements CitySearchService {

    private final GeoSearchApiProperties geoSearchApiProperties;
    private final GeoapifyRateLimiter rateLimiter;
    private final CityLookupService cityLookupService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeoapifyCitySearchService(GeoSearchApiProperties geoSearchApiProperties,
                                     GeoapifyRateLimiter rateLimiter,
                                     CityLookupService cityLookupService) {
        this.geoSearchApiProperties = geoSearchApiProperties;
        this.rateLimiter = rateLimiter;
        this.cityLookupService = cityLookupService;
    }

    @Override
    public List<String> searchCities(String query) {
        log.info("City search request received with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            log.debug("Empty query provided, returning empty list");
            return new ArrayList<>();
        }

        // ── Local-first: check the JSON cache before hitting the API ──
        List<String> localMatches = cityLookupService.searchByPrefix(query);
        if (!localMatches.isEmpty()) {
            log.info("Returning {} city match(es) from local cache for '{}' — skipping Geoapify API", localMatches.size(), query);
            return localMatches;
        }

        log.info("No local match for '{}' — falling back to Geoapify API", query);

        List<String> cities = new ArrayList<>();

        String apiKey = geoSearchApiProperties.getKey() != null ? geoSearchApiProperties.getKey().trim() : null;

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Geoapify API key not configured. Cannot search cities.");
            return cities;
        }

        if (!rateLimiter.tryAcquire()) {
            log.warn("Rate limit exceeded. Cannot search cities. Remaining calls: {}", rateLimiter.getRemainingCalls());
            return cities;
        }

        try {
            String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.geoapify.com/v1/geocode/autocomplete?text=%s&type=city&format=json&apiKey=%s",
                    encodedQuery,
                    encodedApiKey
            );

            log.debug("Geoapify Autocomplete URL: {}", url.replace(apiKey, "***API_KEY***"));

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder()
                            .uri(new URL(url).toURI())
                            .timeout(Duration.ofSeconds(15))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            log.info("Geoapify Autocomplete HTTP status: {}", response.statusCode());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                JsonNode results = json.get("results");

                if (results != null && results.isArray()) {
                    Set<String> uniqueCities = new LinkedHashSet<>();
                    for (JsonNode result : results) {
                        String cityName = extractCityName(result);
                        if (cityName != null && !cityName.isEmpty()) {
                            uniqueCities.add(cityName);
                            // Cache the country code from the autocomplete result
                            cacheCountryCode(cityName, result);
                        }
                    }
                    cities.addAll(uniqueCities);
                }

                log.info("Found {} cities for query: {}", cities.size(), query);
                log.debug("Matching cities: {}", cities);
            } else {
                log.warn("Geoapify API returned HTTP status: {}", response.statusCode());
                log.warn("Response body: {}", response.body());
            }

        } catch (Exception e) {
            log.error("Failed to search cities using Geoapify API. Query: {}. Error: {}", query, e.getMessage(), e);
        }

        return cities;
    }

    /**
     * Cache the country code from the autocomplete result into the city lookup.
     * This allows the /find endpoint to use country-filtered search for accurate results.
     */
    private void cacheCountryCode(String cityName, JsonNode result) {
        try {
            String countryCode = null;
            JsonNode countryCodeNode = result.get("country_code");
            if (countryCodeNode != null && !countryCodeNode.isNull()) {
                countryCode = countryCodeNode.asText().toLowerCase();
            }

            if (countryCode == null || countryCode.isEmpty()) {
                return;
            }

            // Only update if not already in cache, or if cache entry has no country code
            var existing = cityLookupService.lookup(cityName);
            if (existing.isEmpty() || existing.get().getCountryCode() == null) {
                CityGeoInfo geoInfo = existing.orElse(new CityGeoInfo());
                geoInfo.setCountryCode(countryCode);

                // Also capture lat/lon/timezone if available from autocomplete
                if (result.has("lat") && result.has("lon")) {
                    geoInfo.setLatitude(result.get("lat").asDouble());
                    geoInfo.setLongitude(result.get("lon").asDouble());
                }
                if (result.has("timezone")) {
                    JsonNode tzNode = result.get("timezone");
                    if (tzNode.has("name")) {
                        geoInfo.setTimezone(tzNode.get("name").asText());
                    }
                }

                cityLookupService.addCity(cityName, geoInfo);
                log.info("Cached city info from autocomplete: '{}' -> countryCode={}, lat={}, lon={}, tz={}",
                        cityName, countryCode, geoInfo.getLatitude(), geoInfo.getLongitude(), geoInfo.getTimezone());
            }
        } catch (Exception e) {
            log.debug("Could not cache country code for '{}': {}", cityName, e.getMessage());
        }
    }

    private String extractCityName(JsonNode result) {
        JsonNode cityNode = result.get("city");
        if (cityNode != null && !cityNode.isNull() && !cityNode.asText().isEmpty()) {
            return cityNode.asText();
        }

        JsonNode formattedNode = result.get("formatted");
        if (formattedNode != null && !formattedNode.isNull()) {
            return formattedNode.asText().split(",")[0].trim();
        }

        return null;
    }
}
