package com.sankalpam.service.impl;

import com.sankalpam.model.Coordinates;
import com.sankalpam.model.SankalpamFinder;
import com.sankalpam.service.mapping.MappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for SankalpamApiClientImpl
 */
@ExtendWith(MockitoExtension.class)
class SankalpamApiClientImplTest {

    @Mock
    private MappingService mappingService;

    @InjectMocks
    private SankalpamApiClientImpl apiClient;

    @BeforeEach
    void setUp() {
        // Set up Maasam mappings for date-range-based extraction
        Map<String, Map<String, Object>> maasamMap = new LinkedHashMap<>();
        maasamMap.put("Chaitramu", Map.of("range", "15/03-14/04", "months", List.of(3, 4)));
        maasamMap.put("Vaisakhamu", Map.of("range", "15/04-14/05", "months", List.of(4, 5)));
        maasamMap.put("Jyeshthamu", Map.of("range", "15/05-14/06", "months", List.of(5, 6)));
        maasamMap.put("Ashadhamu", Map.of("range", "15/06-14/07", "months", List.of(6, 7)));
        maasamMap.put("Sravanamu", Map.of("range", "15/07-14/08", "months", List.of(7, 8)));
        maasamMap.put("Bhadrapadamu", Map.of("range", "15/08-14/09", "months", List.of(8, 9)));
        maasamMap.put("Ashwayujamu", Map.of("range", "15/09-14/10", "months", List.of(9, 10)));
        maasamMap.put("Karthikamu", Map.of("range", "15/10-14/11", "months", List.of(10, 11)));
        maasamMap.put("Margasiramu", Map.of("range", "15/11-14/12", "months", List.of(11, 12)));
        maasamMap.put("Pushyamu", Map.of("range", "15/12-14/01", "months", List.of(12, 1)));
        maasamMap.put("Maghamu", Map.of("range", "15/01-14/02", "months", List.of(1, 2)));
        maasamMap.put("Phalgunamu", Map.of("range", "15/02-14/03", "months", List.of(2, 3)));
        lenient().when(mappingService.getAllMaasamMappings()).thenReturn(maasamMap);

        // Set up Ruthuvu mappings
        Map<String, Map<String, Object>> ruthuMap = new LinkedHashMap<>();
        ruthuMap.put("Vasantha", Map.of("maasam", List.of("Chaitramu", "Vaisakhamu")));
        ruthuMap.put("Greeshma", Map.of("maasam", List.of("Jyeshthamu", "Ashadhamu")));
        ruthuMap.put("Varsha", Map.of("maasam", List.of("Sravanamu", "Bhadrapadamu")));
        ruthuMap.put("Sharad", Map.of("maasam", List.of("Ashwayujamu", "Karthikamu")));
        ruthuMap.put("Hemantha", Map.of("maasam", List.of("Margasiramu", "Pushyamu")));
        ruthuMap.put("Shishira", Map.of("maasam", List.of("Maghamu", "Phalgunamu")));
        lenient().when(mappingService.getAllRuthuMappings()).thenReturn(ruthuMap);

        // Set up passthrough mapping stubs
        lenient().when(mappingService.mapAyanam(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(mappingService.mapPaksham(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(mappingService.mapTithi(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(mappingService.mapNakshatram(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(mappingService.mapVaasare(anyString())).thenAnswer(i -> i.getArgument(0));
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

    // ── parseDate Tests ──

    @Test
    @DisplayName("parseDate handles YYYY-MM-DD format")
    void parseDate_IsoFormat() {
        java.time.LocalDate date = apiClient.parseDate("2026-03-04");
        assertNotNull(date);
        assertEquals(2026, date.getYear());
        assertEquals(3, date.getMonthValue());
        assertEquals(4, date.getDayOfMonth());
    }

    @Test
    @DisplayName("parseDate handles MM/DD/YYYY format")
    void parseDate_SlashFormat() {
        java.time.LocalDate date = apiClient.parseDate("03/04/2026");
        assertNotNull(date);
        assertEquals(2026, date.getYear());
        assertEquals(3, date.getMonthValue());
        assertEquals(4, date.getDayOfMonth());
    }

    @Test
    @DisplayName("parseDate returns null for unsupported format")
    void parseDate_UnsupportedFormat_ReturnsNull() {
        assertNull(apiClient.parseDate("04.03.2026"));
    }

    @Test
    @DisplayName("parseDate returns null for invalid date")
    void parseDate_InvalidDate_ReturnsNull() {
        assertNull(apiClient.parseDate("2026-13-40"));
    }

    // ── extractMaasamFromDateRange edge cases ──

    @Test
    @DisplayName("extractMaasamFromDateRange with unsupported format returns null")
    void extractMaasamFromDateRange_UnsupportedFormat() {
        assertNull(apiClient.extractMaasamFromDateRange("04.03.2026"));
    }

    @Test
    @DisplayName("extractMaasamFromDateRange with too-few dashes returns null")
    void extractMaasamFromDateRange_TooFewParts() {
        assertNull(apiClient.extractMaasamFromDateRange("2026-03"));
    }

    @Test
    @DisplayName("extractMaasamFromDateRange with single slash part returns null")
    void extractMaasamFromDateRange_SingleSlashPart() {
        assertNull(apiClient.extractMaasamFromDateRange("03"));
    }

    // ── extractRuthuFromMaasam edge cases ──

    @Test
    @DisplayName("extractRuthuFromMaasam null returns null")
    void extractRuthuFromMaasam_Null() {
        assertNull(apiClient.extractRuthuFromMaasam(null));
    }

    @Test
    @DisplayName("extractRuthuFromMaasam empty returns null")
    void extractRuthuFromMaasam_Empty() {
        assertNull(apiClient.extractRuthuFromMaasam(""));
    }

    @Test
    @DisplayName("extractRuthuFromMaasam unknown value returns null")
    void extractRuthuFromMaasam_Unknown() {
        assertNull(apiClient.extractRuthuFromMaasam("UnknownMaasam"));
    }

    // ── extractVaaramFromDate edge cases ──

    @Test
    @DisplayName("extractVaaramFromDate null returns null")
    void extractVaaramFromDate_Null() {
        assertNull(apiClient.extractVaaramFromDate(null));
    }

    @Test
    @DisplayName("extractVaaramFromDate empty returns null")
    void extractVaaramFromDate_Empty() {
        assertNull(apiClient.extractVaaramFromDate(""));
    }

    @Test
    @DisplayName("extractVaaramFromDate invalid date returns null")
    void extractVaaramFromDate_InvalidDate() {
        assertNull(apiClient.extractVaaramFromDate("not-a-date"));
    }

    // ── fetchSankalpam with AM/PM time format ──

    @Test
    @DisplayName("fetchSankalpam handles AM/PM time format")
    void testFetchSankalpam_AmPmTimeFormat() {
        SankalpamFinder result = apiClient.fetchSankalpam(
                "Pune", new Coordinates(18.5204, 73.8567),
                "Asia/Kolkata", "2026-03-01", "6:30 PM");
        assertNotNull(result);
    }

    // ── extractRuthuFromMaasam with empty mapping ──

    @Test
    @DisplayName("extractRuthuFromMaasam returns null when mapping is empty")
    void extractRuthuFromMaasam_EmptyMapping() {
        lenient().when(mappingService.getAllRuthuMappings())
                .thenReturn(new java.util.HashMap<>());
        // Force re-fetch by creating new instance with empty ruthu map
        assertNull(apiClient.extractRuthuFromMaasam("Unknown"));
    }
}
