package com.sankalpam.service.mapping;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonMappingLoader Tests")
class JsonMappingLoaderTest {

    @BeforeAll
    static void init() {
        // Ensure the loader has read the JSON files from classpath
        new JsonMappingLoader(new PathMatchingResourcePatternResolver());
    }

    // ── getMappedValue Tests ──

    @Test
    @DisplayName("getMappedValue returns null for null value")
    void getMappedValue_NullValue_ReturnsNull() {
        assertNull(JsonMappingLoader.getMappedValue("Maasam", null));
    }

    @Test
    @DisplayName("getMappedValue returns null for empty value")
    void getMappedValue_EmptyValue_ReturnsNull() {
        assertNull(JsonMappingLoader.getMappedValue("Maasam", ""));
    }

    @Test
    @DisplayName("getMappedValue returns null for blank value")
    void getMappedValue_BlankValue_ReturnsNull() {
        assertNull(JsonMappingLoader.getMappedValue("Maasam", "   "));
    }

    @Test
    @DisplayName("getMappedValue returns null for unknown attribute")
    void getMappedValue_UnknownAttribute_ReturnsNull() {
        assertNull(JsonMappingLoader.getMappedValue("NonExistent", "somevalue"));
    }

    @Test
    @DisplayName("getMappedValue case-insensitive lookup finds Vaasare")
    void getMappedValue_CaseInsensitive_FindsValue() {
        // Vaasare.json has simple string mappings like "MONDAY": "Indu"
        String result = JsonMappingLoader.getMappedValue("Vaasare", "monday");
        assertNotNull(result, "Case-insensitive lookup should find MONDAY");
    }

    @Test
    @DisplayName("getMappedValue exact match for Vaasare")
    void getMappedValue_ExactMatch_FindsValue() {
        String result = JsonMappingLoader.getMappedValue("Vaasare", "MONDAY");
        assertNotNull(result);
    }

    @Test
    @DisplayName("getMappedValue for Maasam returns key name for complex object")
    void getMappedValue_ComplexObject_ReturnsKeyName() {
        // Maasam entries are complex objects (with range/months), so getMappedValue
        // returns the key name when the value is not a simple String
        String result = JsonMappingLoader.getMappedValue("Maasam", "Phalgunamu");
        assertNotNull(result);
    }

    // ── hasMapping Tests ──

    @Test
    @DisplayName("hasMapping returns true for loaded attributes")
    void hasMapping_Loaded_ReturnsTrue() {
        assertTrue(JsonMappingLoader.hasMapping("Maasam"));
        assertTrue(JsonMappingLoader.hasMapping("Vaasare"));
        assertTrue(JsonMappingLoader.hasMapping("Ruthuvu"));
    }

    @Test
    @DisplayName("hasMapping returns false for non-existent attribute")
    void hasMapping_NotLoaded_ReturnsFalse() {
        assertFalse(JsonMappingLoader.hasMapping("DoesNotExist"));
    }

    // ── getAllMappings Tests ──

    @Test
    @DisplayName("getAllMappings returns non-empty map")
    void getAllMappings_ReturnsData() {
        Map<String, Map<String, Object>> all = JsonMappingLoader.getAllMappings();
        assertNotNull(all);
        assertFalse(all.isEmpty());
    }

    @Test
    @DisplayName("getAllMappings returns a defensive copy")
    void getAllMappings_ReturnsDefensiveCopy() {
        Map<String, Map<String, Object>> copy1 = JsonMappingLoader.getAllMappings();
        copy1.put("Injected", Map.of());
        Map<String, Map<String, Object>> copy2 = JsonMappingLoader.getAllMappings();
        assertFalse(copy2.containsKey("Injected"), "Modifying the returned map should not affect the original");
    }

    // ── printMappingInfo Tests ──

    @Test
    @DisplayName("printMappingInfo should not throw")
    void printMappingInfo_NoException() {
        assertDoesNotThrow(JsonMappingLoader::printMappingInfo);
    }
}

