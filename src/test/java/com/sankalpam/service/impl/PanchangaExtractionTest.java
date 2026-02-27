package com.sankalpam.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Maasam, Ruthuvu, and Vaaram extraction logic
 */
@DisplayName("Panchanga Field Extraction Tests")
class PanchangaExtractionTest {

    private SankalpamApiClientImpl apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new SankalpamApiClientImpl();
    }

    // ========== MAASAM EXTRACTION TESTS ==========

    @DisplayName("Maasam: Phalgunamu extraction for date 2026-02-26")
    @Test
    void testExtractMaasamFromDateRange_Phalgunamu() {
        // Arrange
        String date = "2026-02-26";  // February 26, 2026 - Falls in Phalgunamu (15/02-14/03)

        // Act
        String maasam = apiClient.extractMaasamFromDateRange(date);

        // Assert
        assertNotNull(maasam, "Maasam should not be null");
        assertEquals("Phalgunamu", maasam, "Date 2026-02-26 should be in Phalgunamu range (15/02-14/03)");
    }

    @DisplayName("Maasam: Maghamu extraction for date 2026-01-27")
    @Test
    void testExtractMaasamFromDateRange_Maghamu() {
        // Arrange
        String date = "2026-01-27";  // January 27, 2026 - Falls in Maghamu (15/01-14/02)

        // Act
        String maasam = apiClient.extractMaasamFromDateRange(date);

        // Assert
        assertNotNull(maasam, "Maasam should not be null");
        assertEquals("Maghamu", maasam, "Date 2026-01-27 should be in Maghamu range (15/01-14/02)");
    }

    @DisplayName("Maasam: Chaitramu extraction for date 2026-03-20")
    @Test
    void testExtractMaasamFromDateRange_Chaitramu() {
        // Arrange
        String date = "2026-03-20";  // March 20, 2026 - Falls in Chaitramu (15/03-14/04)

        // Act
        String maasam = apiClient.extractMaasamFromDateRange(date);

        // Assert
        assertNotNull(maasam, "Maasam should not be null");
        assertEquals("Chaitramu", maasam, "Date 2026-03-20 should be in Chaitramu range (15/03-14/04)");
    }

    @ParameterizedTest(name = "Date {0} should extract Maasam: {1}")
    @DisplayName("Maasam extraction for various dates")
    @CsvSource({
        "2026-02-15, Phalgunamu",  // Start of Phalgunamu range
        "2026-02-26, Phalgunamu",  // Middle of Phalgunamu range
        "2026-03-14, Phalgunamu",  // End of Phalgunamu range
        "2026-03-15, Chaitramu",   // Start of Chaitramu range
        "2026-01-20, Maghamu",     // Middle of Maghamu range
        "2026-04-10, Chaitramu",   // Middle of Chaitramu range
        "2026-05-10, Vaisakhamu",  // Vaisakhamu range
        "2026-12-20, Pushyamu"     // Pushyamu crosses year boundary
    })
    void testExtractMaasamFromDateRange_VariousDates(String date, String expectedMaasam) {
        // Act
        String maasam = apiClient.extractMaasamFromDateRange(date);

        // Assert
        assertNotNull(maasam, "Maasam should not be null for date: " + date);
        assertEquals(expectedMaasam, maasam, "Date " + date + " should extract " + expectedMaasam);
    }

    // ========== VAARAM EXTRACTION TESTS ==========

    @DisplayName("Vaaram: Bhruspati extraction for Thursday (2026-02-26)")
    @Test
    void testExtractVaaramFromDate_Thursday() {
        // Arrange
        String date = "2026-02-26";  // This is a Thursday

        // Act
        String vaaram = apiClient.extractVaaramFromDate(date);

        // Assert
        assertNotNull(vaaram, "Vaaram should not be null");
        assertEquals("Bhruspati", vaaram, "Thursday should map to Bhruspati");
    }

    @DisplayName("Vaaram: Bhrugu extraction for Friday (2026-02-27)")
    @Test
    void testExtractVaaramFromDate_Friday() {
        // Arrange
        String date = "2026-02-27";  // This is a Friday

        // Act
        String vaaram = apiClient.extractVaaramFromDate(date);

        // Assert
        assertNotNull(vaaram, "Vaaram should not be null");
        assertEquals("Bhrugu", vaaram, "Friday should map to Bhrugu");
    }

    @ParameterizedTest(name = "Date {0} should extract Vaaram: {1}")
    @DisplayName("Vaaram extraction for various days of week")
    @CsvSource({
        "2026-03-01, Bhanu",        // Sunday
        "2026-03-02, Indu",         // Monday
        "2026-03-03, Bhowma",       // Tuesday
        "2026-03-04, Soumya",       // Wednesday
        "2026-03-05, Bhruspati",    // Thursday
        "2026-03-06, Bhrugu",       // Friday
        "2026-03-07, Sthira"        // Saturday
    })
    void testExtractVaaramFromDate_VariousDays(String date, String expectedVaaram) {
        // Act
        String vaaram = apiClient.extractVaaramFromDate(date);

        // Assert
        assertNotNull(vaaram, "Vaaram should not be null for date: " + date);
        assertEquals(expectedVaaram, vaaram, "Date " + date + " should extract " + expectedVaaram);
    }

    // ========== RUTHUVU EXTRACTION TESTS ==========

    @DisplayName("Ruthuvu: Shishira extraction for Maasam Phalgunamu")
    @Test
    void testExtractRuthuFromMaasam_Shishira() {
        // Arrange
        String maasam = "Phalgunamu";  // Phalgunamu falls under Shishira (Maghamu + Phalgunamu)

        // Act
        String ruthuvu = apiClient.extractRuthuFromMaasam(maasam);

        // Assert
        assertNotNull(ruthuvu, "Ruthuvu should not be null");
        assertEquals("Shishira", ruthuvu, "Phalgunamu should map to Shishira ruthu");
    }

    @DisplayName("Ruthuvu: Shishira extraction for Maasam Maghamu")
    @Test
    void testExtractRuthuFromMaasam_ShishiraFromMaghamu() {
        // Arrange
        String maasam = "Maghamu";  // Maghamu also falls under Shishira

        // Act
        String ruthuvu = apiClient.extractRuthuFromMaasam(maasam);

        // Assert
        assertNotNull(ruthuvu, "Ruthuvu should not be null");
        assertEquals("Shishira", ruthuvu, "Maghamu should map to Shishira ruthu");
    }

    @DisplayName("Ruthuvu: Vasantha extraction for Maasam Chaitramu")
    @Test
    void testExtractRuthuFromMaasam_Vasantha() {
        // Arrange
        String maasam = "Chaitramu";  // Chaitramu falls under Vasantha (Chaitramu + Vaisakhamu)

        // Act
        String ruthuvu = apiClient.extractRuthuFromMaasam(maasam);

        // Assert
        assertNotNull(ruthuvu, "Ruthuvu should not be null");
        assertEquals("Vasantha", ruthuvu, "Chaitramu should map to Vasantha ruthu");
    }

    @ParameterizedTest(name = "Maasam {0} should extract Ruthuvu: {1}")
    @DisplayName("Ruthuvu extraction for various Maasams")
    @CsvSource({
        "Phalgunamu, Shishira",
        "Maghamu, Shishira",
        "Chaitramu, Vasantha",
        "Vaisakhamu, Vasantha",
        "Jyeshthamu, Greeshma",
        "Ashadhamu, Greeshma",
        "Sravanamu, Varsha",
        "Bhadrapadamu, Varsha",
        "Ashwayujamu, Sharad",
        "Karthikamu, Sharad",
        "Margasiramu, Hemantha",
        "Pushyamu, Hemantha"
    })
    void testExtractRuthuFromMaasam_VariousMaasams(String maasam, String expectedRuthuvu) {
        // Act
        String ruthuvu = apiClient.extractRuthuFromMaasam(maasam);

        // Assert
        assertNotNull(ruthuvu, "Ruthuvu should not be null for Maasam: " + maasam);
        assertEquals(expectedRuthuvu, ruthuvu, "Maasam " + maasam + " should map to " + expectedRuthuvu);
    }

    // ========== INTEGRATION TESTS ==========

    @DisplayName("Integration: Complete Panchanga extraction for 2026-02-26")
    @Test
    void testCompletePanchangaExtraction_Feb26_2026() {
        // Arrange
        String date = "2026-02-26";  // Thursday

        // Act
        String maasam = apiClient.extractMaasamFromDateRange(date);
        String vaaram = apiClient.extractVaaramFromDate(date);
        String ruthuvu = apiClient.extractRuthuFromMaasam(maasam);

        // Assert
        assertNotNull(maasam, "Maasam should not be null");
        assertNotNull(vaaram, "Vaaram should not be null");
        assertNotNull(ruthuvu, "Ruthuvu should not be null");

        assertEquals("Phalgunamu", maasam, "Should extract Phalgunamu for 2026-02-26");
        assertEquals("Bhruspati", vaaram, "Should extract Bhruspati for Thursday");
        assertEquals("Shishira", ruthuvu, "Should extract Shishira for Phalgunamu");
    }

    @DisplayName("Integration: Complete Panchanga extraction for 2026-03-20")
    @Test
    void testCompletePanchangaExtraction_Mar20_2026() {
        // Arrange
        String date = "2026-03-20";  // Friday

        // Act
        String maasam = apiClient.extractMaasamFromDateRange(date);
        String vaaram = apiClient.extractVaaramFromDate(date);
        String ruthuvu = apiClient.extractRuthuFromMaasam(maasam);

        // Assert
        assertNotNull(maasam, "Maasam should not be null");
        assertNotNull(vaaram, "Vaaram should not be null");
        assertNotNull(ruthuvu, "Ruthuvu should not be null");

        assertEquals("Chaitramu", maasam, "Should extract Chaitramu for 2026-03-20");
        assertEquals("Bhrugu", vaaram, "Should extract Bhrugu for Friday");
        assertEquals("Vasantha", ruthuvu, "Should extract Vasantha for Chaitramu");
    }

    // ========== EDGE CASE TESTS ==========

    @DisplayName("Edge case: Null date should return null")
    @Test
    void testExtractMaasamFromDateRange_NullDate() {
        // Act
        String maasam = apiClient.extractMaasamFromDateRange(null);

        // Assert
        assertNull(maasam, "Should return null for null date");
    }

    @DisplayName("Edge case: Empty date should return null")
    @Test
    void testExtractMaasamFromDateRange_EmptyDate() {
        // Act
        String maasam = apiClient.extractMaasamFromDateRange("");

        // Assert
        assertNull(maasam, "Should return null for empty date");
    }

    @DisplayName("Edge case: Both date formats should work")
    @Test
    void testExtractMaasamFromDateRange_BothDateFormats() {
        // Arrange
        String dateYYYYMMDD = "2026-02-26";
        String dateMMDDYYYY = "02/26/2026";

        // Act
        String maasam1 = apiClient.extractMaasamFromDateRange(dateYYYYMMDD);
        String maasam2 = apiClient.extractMaasamFromDateRange(dateMMDDYYYY);

        // Assert
        assertNotNull(maasam1, "Should parse YYYY-MM-DD format");
        assertNotNull(maasam2, "Should parse MM/DD/YYYY format");
        assertEquals(maasam1, maasam2, "Both date formats should produce same result");
        assertEquals("Phalgunamu", maasam1, "Both should extract Phalgunamu");
    }
}

