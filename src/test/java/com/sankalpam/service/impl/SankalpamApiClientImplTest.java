package com.sankalpam.service.impl;

import com.sankalpam.model.Coordinates;
import com.sankalpam.model.SankalpamFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SankalpamApiClientImpl
 */
class SankalpamApiClientImplTest {

    private SankalpamApiClientImpl apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new SankalpamApiClientImpl();
    }

    @Test
    @DisplayName("Test fetchSankalpam with Mumbai coordinates")
    void testFetchSankalpam_Mumbai() {
        // Arrange
        String city = "Mumbai";
        Coordinates coords = new Coordinates(19.0760, 72.8777);
        String timezone = "Asia/Kolkata";
        String date = "2026-02-24";
        String time = "18:30";

        // Act
        SankalpamFinder result = apiClient.fetchSankalpam(city, coords, timezone, date, time);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(city, result.getCity(), "City should match");
        assertEquals(date, result.getDate(), "Date should match");
        assertEquals(time, result.getTime(), "Time should match");

        // Verify Panchanga fields are populated
        assertNotNull(result.getSamvatsaram(), "Samvatsaram should not be null");
        assertNotNull(result.getAyanam(), "Ayanam should not be null");
        assertNotNull(result.getRuthu(), "Ruthu should not be null");
        assertNotNull(result.getMasam(), "Masam should not be null");
        assertNotNull(result.getPaksham(), "Paksham should not be null");
        assertNotNull(result.getTithi(), "Tithi should not be null");
        assertNotNull(result.getVaasaram(), "Vaasaram should not be null");
        assertNotNull(result.getNakshatram(), "Nakshatram should not be null");
        assertNotNull(result.getSunrise(), "Sunrise should not be null");
        assertNotNull(result.getSunset(), "Sunset should not be null");

        System.out.println("✅ Test passed for Mumbai");
        System.out.println("   Samvatsaram: " + result.getSamvatsaram());
        System.out.println("   Masam: " + result.getMasam());
        System.out.println("   Tithi: " + result.getTithi());
    }

    @Test
    @DisplayName("Test fetchSankalpam with New York coordinates")
    void testFetchSankalpam_NewYork() {
        // Arrange
        String city = "New York";
        Coordinates coords = new Coordinates(40.7128, -74.0060);
        String timezone = "America/New_York";
        String date = "2026-02-24";
        String time = "10:00";

        // Act
        SankalpamFinder result = apiClient.fetchSankalpam(city, coords, timezone, date, time);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(city, result.getCity());
        assertNotNull(result.getSamvatsaram());

        System.out.println("✅ Test passed for New York");
    }

    @Test
    @DisplayName("Test fetchSankalpam with invalid coordinates - should use fallback")
    void testFetchSankalpam_InvalidCoords() {
        // Arrange
        String city = "TestCity";
        Coordinates coords = new Coordinates(0.0, 0.0);
        String timezone = "UTC";
        String date = "2026-02-24";
        String time = "12:00";

        // Act
        SankalpamFinder result = apiClient.fetchSankalpam(city, coords, timezone, date, time);

        // Assert - Should get fallback data, not crash
        assertNotNull(result, "Result should not be null even with invalid coords");
        assertEquals(city, result.getCity());
        assertEquals(date, result.getDate());

        System.out.println("✅ Test passed - fallback data used");
    }

    @Test
    @DisplayName("Test fetchSankalpam with different time formats")
    void testFetchSankalpam_DifferentTimes() {
        String city = "Chennai";
        Coordinates coords = new Coordinates(13.0827, 80.2707);
        String timezone = "Asia/Kolkata";
        String date = "2026-03-15";

        // Test morning time
        SankalpamFinder morning = apiClient.fetchSankalpam(city, coords, timezone, date, "06:00");
        assertNotNull(morning);
        assertEquals("06:00", morning.getTime());

        // Test evening time
        SankalpamFinder evening = apiClient.fetchSankalpam(city, coords, timezone, date, "18:00");
        assertNotNull(evening);
        assertEquals("18:00", evening.getTime());

        System.out.println("✅ Test passed for different times");
    }

    @Test
    @DisplayName("Test that all Panchanga fields have valid values")
    void testFetchSankalpam_AllFieldsPopulated() {
        // Arrange
        String city = "Bangalore";
        Coordinates coords = new Coordinates(12.9716, 77.5946);
        String timezone = "Asia/Kolkata";
        String date = "2026-02-24";
        String time = "15:30";

        // Act
        SankalpamFinder result = apiClient.fetchSankalpam(city, coords, timezone, date, time);

        // Assert - Check all fields are populated with non-empty strings
        assertNotNull(result);
        assertFalse(result.getSamvatsaram().isEmpty(), "Samvatsaram should not be empty");
        assertFalse(result.getAyanam().isEmpty(), "Ayanam should not be empty");
        assertFalse(result.getRuthu().isEmpty(), "Ruthu should not be empty");
        assertFalse(result.getMasam().isEmpty(), "Masam should not be empty");
        assertFalse(result.getPaksham().isEmpty(), "Paksham should not be empty");
        assertFalse(result.getTithi().isEmpty(), "Tithi should not be empty");
        assertFalse(result.getVaasaram().isEmpty(), "Vaasaram should not be empty");
        assertFalse(result.getNakshatram().isEmpty(), "Nakshatram should not be empty");
        assertFalse(result.getSunrise().isEmpty(), "Sunrise should not be empty");
        assertFalse(result.getSunset().isEmpty(), "Sunset should not be empty");

        System.out.println("✅ All Panchanga fields are populated");
        System.out.println("\nPanchanga Details:");
        System.out.println("  Samvatsaram: " + result.getSamvatsaram());
        System.out.println("  Ayanam: " + result.getAyanam());
        System.out.println("  Ruthu: " + result.getRuthu());
        System.out.println("  Masam: " + result.getMasam());
        System.out.println("  Paksham: " + result.getPaksham());
        System.out.println("  Tithi: " + result.getTithi());
        System.out.println("  Vaasaram: " + result.getVaasaram());
        System.out.println("  Nakshatram: " + result.getNakshatram());
        System.out.println("  Sunrise: " + result.getSunrise());
        System.out.println("  Sunset: " + result.getSunset());
    }
}

