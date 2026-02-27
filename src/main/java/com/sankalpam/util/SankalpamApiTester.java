package com.sankalpam.util;

import com.sankalpam.model.Coordinates;
import com.sankalpam.model.SankalpamFinder;
import com.sankalpam.service.impl.SankalpamApiClientImpl;

/**
 * Standalone tester for SankalpamApiClient.fetchSankalpam method
 * Run this directly to test the external Panchanga API
 */
public class SankalpamApiTester {

    public static void main(String[] args) {
        System.out.println("\n================================================================================");
        System.out.println("     Sankalpam API fetchSankalpam Method Tester");
        System.out.println("================================================================================\n");

        SankalpamApiClientImpl apiClient = new SankalpamApiClientImpl();

        // Test Case 1: Mumbai
        System.out.println("Test 1: Mumbai, India");
        System.out.println("--------------------------------------------------------------------------------");
        testFetchSankalpam(
                apiClient,
                "Mumbai",
                new Coordinates(19.0760, 72.8777),
                "Asia/Kolkata",
                "2026-02-24",
                "18:30"
        );

        System.out.println("\n--------------------------------------------------------------------------------\n");

        // Test Case 2: New York
        System.out.println("Test 2: New York, USA");
        System.out.println("--------------------------------------------------------------------------------");
        testFetchSankalpam(
                apiClient,
                "New York",
                new Coordinates(40.7128, -74.0060),
                "America/New_York",
                "2026-02-24",
                "10:00"
        );

        System.out.println("\n--------------------------------------------------------------------------------\n");

        // Test Case 3: Chennai (Different time)
        System.out.println("Test 3: Chennai, India (Morning time)");
        System.out.println("--------------------------------------------------------------------------------");
        testFetchSankalpam(
                apiClient,
                "Chennai",
                new Coordinates(13.0827, 80.2707),
                "Asia/Kolkata",
                "2026-03-15",
                "06:00"
        );

        System.out.println("\n--------------------------------------------------------------------------------\n");

        // Test Case 4: London (Different timezone)
        System.out.println("Test 4: London, UK");
        System.out.println("--------------------------------------------------------------------------------");
        testFetchSankalpam(
                apiClient,
                "London",
                new Coordinates(51.5074, -0.1278),
                "Europe/London",
                "2026-02-24",
                "15:00"
        );

        System.out.println("\n================================================================================");
        System.out.println("                  All Tests Complete");
        System.out.println("================================================================================\n");
    }

    private static void testFetchSankalpam(
            SankalpamApiClientImpl apiClient,
            String city,
            Coordinates coords,
            String timezone,
            String date,
            String time
    ) {
        System.out.println("Input Parameters:");
        System.out.println("  City: " + city);
        System.out.println("  Coordinates: " + coords.lat() + ", " + coords.lng());
        System.out.println("  Timezone: " + timezone);
        System.out.println("  Date: " + date);
        System.out.println("  Time: " + time);
        System.out.println();

        try {
            long startTime = System.currentTimeMillis();

            SankalpamFinder result = apiClient.fetchSankalpam(city, coords, timezone, date, time);

            long duration = System.currentTimeMillis() - startTime;

            if (result != null) {
                System.out.println("SUCCESS! (Response time: " + duration + "ms)");
                System.out.println();
                System.out.println("Panchanga Details:");
                System.out.println("  Samvatsaram: " + result.getSamvatsaram());
                System.out.println("  Ayanam: " + result.getAyanam());
                System.out.println("  Ruthu: " + result.getRuthu());
                System.out.println("  Masam: " + result.getMasam());
                System.out.println("  Paksham: " + result.getPaksham());
                System.out.println("  Tithi: " + result.getTithi());
                System.out.println("  Vaasaram: " + result.getVaasaram());
                System.out.println("  Nakshatram: " + result.getNakshatram());
                System.out.println("  Rasi: " + result.getRasi());
                System.out.println();
                System.out.println("Sunrise: " + result.getSunrise());
                System.out.println("Sunset: " + result.getSunset());

                // Validate all fields are populated
                boolean allValid = true;
                if (result.getSamvatsaram() == null || result.getSamvatsaram().isEmpty()) {
                    System.out.println("WARNING: Samvatsaram is empty");
                    allValid = false;
                }
                if (result.getAyanam() == null || result.getAyanam().isEmpty()) {
                    System.out.println("WARNING: Ayanam is empty");
                    allValid = false;
                }
                if (result.getMasam() == null || result.getMasam().isEmpty()) {
                    System.out.println("WARNING: Masam is empty");
                    allValid = false;
                }

                if (allValid) {
                    System.out.println("\nAll fields validated successfully!");
                }

            } else {
                System.out.println("FAILED: Result is null");
            }

        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e.getClass().getSimpleName());
            System.out.println("   Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

