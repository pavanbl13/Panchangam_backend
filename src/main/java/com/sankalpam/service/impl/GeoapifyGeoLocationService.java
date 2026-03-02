package com.sankalpam.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankalpam.config.GeoSearchApiProperties;
import com.sankalpam.model.CityGeoInfo;
import com.sankalpam.model.Coordinates;
import com.sankalpam.service.CityLookupService;
import com.sankalpam.service.GeoapifyRateLimiter;
import com.sankalpam.service.GeoLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class GeoapifyGeoLocationService implements GeoLocationService {

    private final GeoSearchApiProperties geoSearchApiProperties;
    private final CityLookupService cityLookupService;
    private final GeoapifyRateLimiter rateLimiter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeoapifyGeoLocationService(GeoSearchApiProperties geoSearchApiProperties,
                                       CityLookupService cityLookupService,
                                       GeoapifyRateLimiter rateLimiter) {
        this.geoSearchApiProperties = geoSearchApiProperties;
        this.cityLookupService = cityLookupService;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Coordinates getCoordinates(String city) {
        CityGeoInfo geoInfo = getGeoInfo(city);
        return geoInfo.toCoordinates();
    }

    @Override
    public CityGeoInfo getGeoInfo(String city) {
        // Step 1: Check city lookup cache
        Optional<CityGeoInfo> cached = cityLookupService.lookup(city);
        if (cached.isPresent()) {
            CityGeoInfo info = cached.get();
            // Only use cache if it has valid lat/lon/timezone (not just countryCode)
            if (info.getLatitude() != 0 && info.getLongitude() != 0 && info.getTimezone() != null && !info.getTimezone().isEmpty()) {
                log.info("Using cached geo info for city: {}", city);
                return info;
            }
        }

        // Step 2: Call Geoapify search API with country code filter if available
        log.info("City '{}' not fully cached. Calling Geoapify search API...", city);
        String countryCode = cached.map(CityGeoInfo::getCountryCode).orElse(null);
        CityGeoInfo geoInfo = fetchFromGeoapify(city, countryCode);

        // Step 3: Add to cache for future reference
        if (geoInfo != null) {
            cityLookupService.addCity(city, geoInfo);
            return geoInfo;
        }

        // Step 4: Fallback if API fails
        log.warn("Geoapify API failed for city: {}. Using default fallback.", city);
        return new CityGeoInfo(19.0760, 72.8777, "Asia/Kolkata");
    }

    private CityGeoInfo fetchFromGeoapify(String city, String countryCode) {
        String apiKey = geoSearchApiProperties.getKey() != null ? geoSearchApiProperties.getKey().trim() : null;

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Geoapify API key not configured. Cannot fetch geo info.");
            return null;
        }

        if (!rateLimiter.tryAcquire()) {
            log.warn("Rate limit exceeded. Cannot fetch geo info for city: {}. Remaining calls: {}", city, rateLimiter.getRemainingCalls());
            return null;
        }

        try {
            // Extract plain city name (before ", Country") for API search text
            String searchText = city.contains(",") ? city.split(",")[0].trim() : city;
            String encodedCity = URLEncoder.encode(searchText, StandardCharsets.UTF_8);

            // Build URL with country code filter if available
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append("https://api.geoapify.com/v1/geocode/search?text=")
                      .append(encodedCity)
                      .append("&lang=en&limit=10&type=city");

            if (countryCode != null && !countryCode.isEmpty()) {
                urlBuilder.append("&filter=countrycode:").append(countryCode.toLowerCase());
                log.info("Using country code filter: {}", countryCode);
            }

            urlBuilder.append("&format=json&apiKey=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8));

            String url = urlBuilder.toString();
            log.debug("Geoapify Search URL: {}", url.replace(apiKey, "***API_KEY***"));

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

            log.info("Geoapify Search HTTP status: {}", response.statusCode());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                JsonNode results = json.get("results");

                if (results != null && results.isArray() && !results.isEmpty()) {
                    JsonNode first = results.get(0);

                    double lat = first.has("lat") ? first.get("lat").asDouble() : 0;
                    double lon = first.has("lon") ? first.get("lon").asDouble() : 0;
                    String timezone = extractTimezone(first);
                    String resolvedCountryCode = extractCountryCode(first);

                    log.info("Geoapify result for '{}': lat={}, lon={}, timezone={}, countryCode={}",
                            city, lat, lon, timezone, resolvedCountryCode);

                    return new CityGeoInfo(lat, lon, timezone, resolvedCountryCode);
                } else {
                    log.warn("No results found from Geoapify for city: {}", city);
                }
            } else {
                log.warn("Geoapify API returned HTTP status: {}", response.statusCode());
                log.warn("Response body: {}", response.body());
            }

        } catch (Exception e) {
            log.error("Failed to fetch geo info from Geoapify for city: {}. Error: {}", city, e.getMessage(), e);
        }

        return null;
    }

    private String extractTimezone(JsonNode result) {
        JsonNode tzNode = result.get("timezone");
        if (tzNode != null) {
            JsonNode nameNode = tzNode.get("name");
            if (nameNode != null && !nameNode.isNull()) {
                return nameNode.asText();
            }
        }
        log.warn("No timezone found in Geoapify response. Using UTC as fallback.");
        return "UTC";
    }

    private String extractCountryCode(JsonNode result) {
        JsonNode ccNode = result.get("country_code");
        if (ccNode != null && !ccNode.isNull()) {
            return ccNode.asText().toLowerCase();
        }
        return null;
    }
}
