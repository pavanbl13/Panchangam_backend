package com.sankalpam.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankalpam.config.GoogleApiProperties;
import com.sankalpam.model.Coordinates;
import com.sankalpam.service.GeoLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
public class GoogleGeoLocationService implements GeoLocationService {

    private final String apiKey;

    // Fallback coordinates for common cities when API is not available
    private static final Map<String, Coordinates> CITY_COORDINATES = Map.ofEntries(
        Map.entry("mumbai", new Coordinates(19.0760, 72.8777)),
        Map.entry("delhi", new Coordinates(28.7041, 77.1025)),
        Map.entry("bangalore", new Coordinates(12.9716, 77.5946)),
        Map.entry("chennai", new Coordinates(13.0827, 80.2707)),
        Map.entry("kolkata", new Coordinates(22.5726, 88.3639)),
        Map.entry("hyderabad", new Coordinates(17.3850, 78.4867)),
        Map.entry("pune", new Coordinates(18.5204, 73.8567)),
        Map.entry("new york", new Coordinates(40.7128, -74.0060)),
        Map.entry("london", new Coordinates(51.5074, -0.1278)),
        Map.entry("singapore", new Coordinates(1.3521, 103.8198)),
        Map.entry("sydney", new Coordinates(-33.8688, 151.2093)),
        Map.entry("tokyo", new Coordinates(35.6762, 139.6503))
    );

    public GoogleGeoLocationService(GoogleApiProperties properties) {
        this.apiKey = properties.getKey();
    }

    @Override
    public Coordinates getCoordinates(String city) {
        // Check if API key is configured
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("YOUR_GOOGLE_API_KEY")) {
            log.warn("Google API key not configured properly. Key: {}", apiKey != null ? maskKey(apiKey) : "null");
            return getFallbackCoordinates(city);
        }

        log.info("Using Google Geocoding API for city: {} (API key: {}...)", city, maskKey(apiKey));

        try {
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                    URLEncoder.encode(city, StandardCharsets.UTF_8),
                    apiKey
            );

            log.debug("Geocoding API URL: {}", url.replace(apiKey, "***API_KEY***"));

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                            HttpResponse.BodyHandlers.ofString());

            log.info("Geocoding API HTTP status: {}", response.statusCode());

            JsonNode json = new ObjectMapper().readTree(response.body());
            String status = json.get("status").asText();

            log.info("Geocoding API response status: {}", status);

            if ("OK".equals(status)) {
                JsonNode loc = json.at("/results/0/geometry/location");

                if (loc.isMissingNode()) {
                    log.warn("No location found in Google API response. Using fallback coordinates for: {}", city);
                    return getFallbackCoordinates(city);
                }

                double lat = loc.get("lat").asDouble();
                double lng = loc.get("lng").asDouble();
                log.info("SUCCESS: Got coordinates from Google API - lat={}, lng={}", lat, lng);
                return new Coordinates(lat, lng);

            } else {
                log.warn("Google API returned status: {}. Using fallback coordinates for: {}", status, city);
                if (json.has("error_message")) {
                    log.warn("Google API error message: {}", json.get("error_message").asText());
                }
                return getFallbackCoordinates(city);
            }

        } catch (Exception e) {
            log.error("Failed to fetch coordinates from Google API for city: {}. Using fallback. Error: {}",
                    city, e.getMessage(), e);
            return getFallbackCoordinates(city);
        }
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 10) return "***";
        return key.substring(0, 7) + "...";
    }

    private Coordinates getFallbackCoordinates(String city) {
        String cityLower = city.toLowerCase().trim();

        // Try exact match first
        if (CITY_COORDINATES.containsKey(cityLower)) {
            log.info("Using fallback coordinates for known city: {}", city);
            return CITY_COORDINATES.get(cityLower);
        }

        // Try partial match
        for (Map.Entry<String, Coordinates> entry : CITY_COORDINATES.entrySet()) {
            if (cityLower.contains(entry.getKey()) || entry.getKey().contains(cityLower)) {
                log.info("Using fallback coordinates for partially matched city: {} -> {}", city, entry.getKey());
                return entry.getValue();
            }
        }

        // Default to Mumbai coordinates if no match found
        log.warn("No fallback coordinates found for: {}. Using default (Mumbai)", city);
        return new Coordinates(19.0760, 72.8777);
    }
}