package com.sankalpam.service.impl;

import com.sankalpam.service.mapping.JsonMappingLoader;
import com.sankalpam.service.mapping.MappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MappingServiceImpl Tests")
class MappingServiceImplTest {

    private MappingServiceImpl mappingService;

    @BeforeEach
    void setUp() {
        // Initialize JsonMappingLoader so static lookup is populated
        new JsonMappingLoader(new PathMatchingResourcePatternResolver());
        mappingService = new MappingServiceImpl();
    }

    // ── mapValue Tests ──

    @Test
    @DisplayName("mapValue should return null for null value")
    void mapValue_NullValue_ReturnsNull() {
        String result = mappingService.mapValue("Maasam", null);
        assertNull(result);
    }

    @Test
    @DisplayName("mapValue should return empty for empty value")
    void mapValue_EmptyValue_ReturnsEmpty() {
        String result = mappingService.mapValue("Maasam", "");
        assertEquals("", result);
    }

    @Test
    @DisplayName("mapValue should return blank for blank value")
    void mapValue_BlankValue_ReturnsBlank() {
        String result = mappingService.mapValue("Maasam", "   ");
        assertEquals("   ", result);
    }

    @Test
    @DisplayName("mapValue should return original for unknown attribute")
    void mapValue_UnknownAttribute_ReturnsOriginal() {
        String result = mappingService.mapValue("NonExistent", "somevalue");
        assertEquals("somevalue", result);
    }

    // ── Passthrough mapping tests ──

    @Test
    @DisplayName("mapAyanam should pass through unchanged")
    void mapAyanam_ReturnsOriginal() {
        assertEquals("uttarAyaNE", mappingService.mapAyanam("uttarAyaNE"));
    }

    @Test
    @DisplayName("mapRuthu should pass through unchanged")
    void mapRuthu_ReturnsOriginal() {
        assertEquals("sisira", mappingService.mapRuthu("sisira"));
    }

    @Test
    @DisplayName("mapTithi should pass through unchanged")
    void mapTithi_ReturnsOriginal() {
        assertEquals("ashtamyAm", mappingService.mapTithi("ashtamyAm"));
    }

    @Test
    @DisplayName("mapPaksham should pass through unchanged")
    void mapPaksham_ReturnsOriginal() {
        assertEquals("shukla", mappingService.mapPaksham("shukla"));
    }

    @Test
    @DisplayName("mapVaaram should pass through unchanged")
    void mapVaaram_ReturnsOriginal() {
        assertEquals("bhouma", mappingService.mapVaaram("bhouma"));
    }

    @Test
    @DisplayName("mapNakshatram should pass through unchanged")
    void mapNakshatram_ReturnsOriginal() {
        assertEquals("rOhinI", mappingService.mapNakshatram("rOhinI"));
    }

    // ── mapMaasam Tests ──

    @Test
    @DisplayName("mapMaasam should use JSON lookup")
    void mapMaasam_DelegatesToMapValue() {
        String result = mappingService.mapMaasam("somevalue");
        assertNotNull(result);
    }

    // ── mapVaasare Tests ──

    @Test
    @DisplayName("mapVaasare should use JSON lookup for MONDAY")
    void mapVaasare_Monday_ReturnsMapping() {
        String result = mappingService.mapVaasare("MONDAY");
        assertNotNull(result);
    }

    // ── hasMapping Tests ──

    @Test
    @DisplayName("hasMapping returns true for known attribute")
    void hasMapping_Maasam_ReturnsTrue() {
        assertTrue(mappingService.hasMapping("Maasam"));
    }

    @Test
    @DisplayName("hasMapping returns false for unknown attribute")
    void hasMapping_Unknown_ReturnsFalse() {
        assertFalse(mappingService.hasMapping("NonExistent"));
    }

    // ── getAllMaasamMappings Tests ──

    @Test
    @DisplayName("getAllMaasamMappings returns non-empty map")
    void getAllMaasamMappings_ReturnsData() {
        var result = mappingService.getAllMaasamMappings();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ── getAllRuthuMappings Tests ──

    @Test
    @DisplayName("getAllRuthuMappings returns non-empty map")
    void getAllRuthuMappings_ReturnsData() {
        var result = mappingService.getAllRuthuMappings();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}

