package com.sankalpam.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankalpam.config.GoogleApiProperties;
import com.sankalpam.model.Coordinates;
import com.sankalpam.service.TimeZoneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

@Slf4j
@Service
public class GoogleTimeZoneService implements TimeZoneService {

    private final String apiKey;

    public GoogleTimeZoneService(GoogleApiProperties properties) {
        this.apiKey = properties.getKey();
    }

    @Override
    public String getTimeZone(Coordinates coordinates) {
        // Check if API key is configured
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("YOUR_GOOGLE_API_KEY")) {
            log.warn("Google API key not configured properly. Using fallback timezone");
            return getFallbackTimeZone(coordinates);
        }

        log.info("Using Google Timezone API for coordinates: {}", coordinates);

        try {
            long timestamp = Instant.now().getEpochSecond();

            String url = String.format(
                    "https://maps.googleapis.com/maps/api/timezone/json?location=%f,%f&timestamp=%d&key=%s",
                    coordinates.lat(), coordinates.lng(), timestamp, apiKey
            );

            log.debug("Timezone API URL: {}", url.replace(apiKey, "***API_KEY***"));

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                            HttpResponse.BodyHandlers.ofString());

            log.info("Timezone API HTTP status: {}", response.statusCode());

            JsonNode json = new ObjectMapper().readTree(response.body());

            // Check if API returned valid response
            JsonNode status = json.get("status");
            String statusText = status != null ? status.asText() : "UNKNOWN";
            log.info("Timezone API response status: {}", statusText);

            if ("OK".equals(statusText)) {
                JsonNode timeZoneId = json.get("timeZoneId");
                if (timeZoneId == null || timeZoneId.isNull()) {
                    log.warn("No timezone found in API response. Using fallback.");
                    return getFallbackTimeZone(coordinates);
                }

                String tzId = timeZoneId.asText();
                log.info("SUCCESS: Got timezone from Google API - {}", tzId);
                return tzId;

            } else {
                log.warn("Google API returned status: {}. Using fallback timezone", statusText);
                if (json.has("errorMessage")) {
                    log.warn("Google API error: {}", json.get("errorMessage").asText());
                }
                return getFallbackTimeZone(coordinates);
            }

        } catch (Exception e) {
            log.error("Failed to fetch timezone from Google API. Using fallback. Error: {}", e.getMessage(), e);
            return getFallbackTimeZone(coordinates);
        }
    }

    private String getFallbackTimeZone(Coordinates coordinates) {
        double lat = coordinates.lat();
        double lng = coordinates.lng();

        log.info("Determining timezone based on coordinates: lat={}, lng={}", lat, lng);

        // India (roughly)
        if (lat >= 8 && lat <= 35 && lng >= 68 && lng <= 97) {
            return "Asia/Kolkata";
        }

        // USA East Coast
        if (lat >= 25 && lat <= 48 && lng >= -85 && lng <= -67) {
            return "America/New_York";
        }

        // USA West Coast
        if (lat >= 32 && lat <= 49 && lng >= -125 && lng <= -114) {
            return "America/Los_Angeles";
        }

        // UK
        if (lat >= 49 && lat <= 61 && lng >= -8 && lng <= 2) {
            return "Europe/London";
        }

        // Singapore/Malaysia
        if (lat >= 1 && lat <= 8 && lng >= 100 && lng <= 105) {
            return "Asia/Singapore";
        }

        // Australia (East)
        if (lat >= -38 && lat <= -28 && lng >= 144 && lng <= 154) {
            return "Australia/Sydney";
        }

        // Japan
        if (lat >= 30 && lat <= 46 && lng >= 130 && lng <= 146) {
            return "Asia/Tokyo";
        }

        // Default to UTC
        log.warn("Could not determine timezone for coordinates: {}. Using UTC", coordinates);
        return "UTC";
    }
}

