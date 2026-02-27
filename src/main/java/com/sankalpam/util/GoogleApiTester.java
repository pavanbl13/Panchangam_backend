package com.sankalpam.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Utility to test Google API connectivity
 */
@Slf4j
public class GoogleApiTester {

    public static void main(String[] args) {
        String apiKey = "AIzaSyBq4oT-gLWACrC8J76ToCwXRpYKzKIlvag";

        System.out.println("\n================================================================================");
        System.out.println("         Google API Connectivity Test");
        System.out.println("================================================================================\n");

        // Test 1: Geocoding API
        testGeocodingApi(apiKey, "Mumbai");

        System.out.println("\n--------------------------------------------------------------------------------\n");

        // Test 2: Geocoding API with another city
        testGeocodingApi(apiKey, "New York");

        System.out.println("\n--------------------------------------------------------------------------------\n");

        // Test 3: Timezone API
        testTimezoneApi(apiKey, 19.0760, 72.8777); // Mumbai coordinates

        System.out.println("\n================================================================================");
        System.out.println("                   Test Complete");
        System.out.println("================================================================================\n");
    }

    private static void testGeocodingApi(String apiKey, String city) {
        System.out.println("🧪 Testing Geocoding API for: " + city);
        System.out.println("   API Key: " + maskApiKey(apiKey));

        try {
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                    URLEncoder.encode(city, StandardCharsets.UTF_8),
                    apiKey
            );

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                            HttpResponse.BodyHandlers.ofString());

            System.out.println("   HTTP Status: " + response.statusCode());

            JsonNode json = new ObjectMapper().readTree(response.body());
            String status = json.get("status").asText();

            System.out.println("   API Status: " + status);

            if ("OK".equals(status)) {
                JsonNode loc = json.at("/results/0/geometry/location");
                double lat = loc.get("lat").asDouble();
                double lng = loc.get("lng").asDouble();
                String formattedAddress = json.at("/results/0/formatted_address").asText();

                System.out.println("   ✅ SUCCESS!");
                System.out.println("   Coordinates: " + lat + ", " + lng);
                System.out.println("   Address: " + formattedAddress);
            } else {
                System.out.println("   ❌ FAILED!");
                if (json.has("error_message")) {
                    System.out.println("   Error: " + json.get("error_message").asText());
                }
                if ("REQUEST_DENIED".equals(status)) {
                    System.out.println("   ⚠️  API Key may be invalid or restricted");
                    System.out.println("   ⚠️  Check: https://console.cloud.google.com/apis/credentials");
                } else if ("OVER_QUERY_LIMIT".equals(status)) {
                    System.out.println("   ⚠️  API quota exceeded");
                }
            }

        } catch (Exception e) {
            System.out.println("   ❌ EXCEPTION: " + e.getMessage());
        }
    }

    private static void testTimezoneApi(String apiKey, double lat, double lng) {
        System.out.println("🧪 Testing Timezone API");
        System.out.println("   Coordinates: " + lat + ", " + lng);
        System.out.println("   API Key: " + maskApiKey(apiKey));

        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/timezone/json?location=%f,%f&timestamp=%d&key=%s",
                    lat, lng, timestamp, apiKey
            );

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                            HttpResponse.BodyHandlers.ofString());

            System.out.println("   HTTP Status: " + response.statusCode());

            JsonNode json = new ObjectMapper().readTree(response.body());
            String status = json.get("status").asText();

            System.out.println("   API Status: " + status);

            if ("OK".equals(status)) {
                String timeZoneId = json.get("timeZoneId").asText();
                String timeZoneName = json.get("timeZoneName").asText();

                System.out.println("   ✅ SUCCESS!");
                System.out.println("   Timezone ID: " + timeZoneId);
                System.out.println("   Timezone Name: " + timeZoneName);
            } else {
                System.out.println("   ❌ FAILED!");
                if (json.has("errorMessage")) {
                    System.out.println("   Error: " + json.get("errorMessage").asText());
                }
            }

        } catch (Exception e) {
            System.out.println("   ❌ EXCEPTION: " + e.getMessage());
        }
    }

    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) return "***";
        return apiKey.substring(0, 10) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}

