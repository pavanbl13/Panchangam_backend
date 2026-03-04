package com.sankalpam.service.impl;

import com.sankalpam.model.CityGeoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CityLookupServiceImpl Tests")
class CityLookupServiceImplTest {

    private CityLookupServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CityLookupServiceImpl();
        service.init(); // loads cities from classpath lookup/cities.json
    }

    // ── lookup Tests ──

    @Test
    @DisplayName("lookup returns value for known city")
    void lookup_KnownCity_ReturnsPresent() {
        Optional<CityGeoInfo> result = service.lookup("Mumbai");
        assertTrue(result.isPresent());
        assertNotEquals(0, result.get().getLatitude());
    }

    @Test
    @DisplayName("lookup is case-insensitive")
    void lookup_CaseInsensitive_ReturnsPresent() {
        Optional<CityGeoInfo> lower = service.lookup("mumbai");
        Optional<CityGeoInfo> upper = service.lookup("MUMBAI");
        Optional<CityGeoInfo> mixed = service.lookup("Mumbai");
        assertTrue(lower.isPresent());
        assertTrue(upper.isPresent());
        assertTrue(mixed.isPresent());
    }

    @Test
    @DisplayName("lookup returns empty for unknown city")
    void lookup_UnknownCity_ReturnsEmpty() {
        Optional<CityGeoInfo> result = service.lookup("Timbuktu999");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("lookup returns empty for null")
    void lookup_Null_ReturnsEmpty() {
        assertFalse(service.lookup(null).isPresent());
    }

    @Test
    @DisplayName("lookup returns empty for empty string")
    void lookup_Empty_ReturnsEmpty() {
        assertFalse(service.lookup("").isPresent());
    }

    @Test
    @DisplayName("lookup returns empty for blank string")
    void lookup_Blank_ReturnsEmpty() {
        assertFalse(service.lookup("   ").isPresent());
    }

    // ── addCity Tests ──

    @Test
    @DisplayName("addCity adds city to cache and lookup succeeds")
    void addCity_NewCity_CanBeLookedUp() {
        CityGeoInfo info = new CityGeoInfo(28.6139, 77.2090, "Asia/Kolkata", "in");
        service.addCity("Delhi", info);

        Optional<CityGeoInfo> result = service.lookup("Delhi");
        assertTrue(result.isPresent());
        assertEquals(28.6139, result.get().getLatitude(), 0.001);
    }

    @Test
    @DisplayName("addCity ignores null city")
    void addCity_NullCity_Ignored() {
        assertDoesNotThrow(() -> service.addCity(null, new CityGeoInfo()));
    }

    @Test
    @DisplayName("addCity ignores empty city")
    void addCity_EmptyCity_Ignored() {
        assertDoesNotThrow(() -> service.addCity("", new CityGeoInfo()));
    }

    @Test
    @DisplayName("addCity ignores null geoInfo")
    void addCity_NullGeoInfo_Ignored() {
        assertDoesNotThrow(() -> service.addCity("TestCity", null));
    }

    // ── exists Tests ──

    @Test
    @DisplayName("exists returns true for known city")
    void exists_KnownCity_ReturnsTrue() {
        assertTrue(service.exists("sydney"));
    }

    @Test
    @DisplayName("exists returns false for unknown city")
    void exists_UnknownCity_ReturnsFalse() {
        assertFalse(service.exists("Atlantis"));
    }

    @Test
    @DisplayName("exists returns false for null")
    void exists_Null_ReturnsFalse() {
        assertFalse(service.exists(null));
    }

    @Test
    @DisplayName("exists returns false for empty string")
    void exists_Empty_ReturnsFalse() {
        assertFalse(service.exists(""));
    }

    // ── searchByPrefix Tests ──

    @Test
    @DisplayName("searchByPrefix finds matching cities")
    void searchByPrefix_Match_ReturnsCities() {
        List<String> result = service.searchByPrefix("syd");
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(c -> c.toLowerCase().contains("syd")));
    }

    @Test
    @DisplayName("searchByPrefix returns empty for no matches")
    void searchByPrefix_NoMatch_ReturnsEmpty() {
        List<String> result = service.searchByPrefix("zzzznotexist");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("searchByPrefix returns empty for null query")
    void searchByPrefix_Null_ReturnsEmpty() {
        assertTrue(service.searchByPrefix(null).isEmpty());
    }

    @Test
    @DisplayName("searchByPrefix returns empty for empty query")
    void searchByPrefix_Empty_ReturnsEmpty() {
        assertTrue(service.searchByPrefix("").isEmpty());
    }

    @Test
    @DisplayName("searchByPrefix is case-insensitive")
    void searchByPrefix_CaseInsensitive() {
        List<String> lower = service.searchByPrefix("mum");
        List<String> upper = service.searchByPrefix("MUM");
        assertEquals(lower.size(), upper.size());
    }

    // ── getAllCityNames Tests ──

    @Test
    @DisplayName("getAllCityNames returns all cached cities")
    void getAllCityNames_ReturnsAll() {
        List<String> all = service.getAllCityNames();
        assertNotNull(all);
        assertFalse(all.isEmpty());
    }

    @Test
    @DisplayName("getAllCityNames returns sorted list")
    void getAllCityNames_ReturnsSorted() {
        List<String> all = service.getAllCityNames();
        for (int i = 1; i < all.size(); i++) {
            assertTrue(all.get(i).compareTo(all.get(i - 1)) >= 0,
                    "List should be sorted: " + all.get(i - 1) + " vs " + all.get(i));
        }
    }

    @Test
    @DisplayName("getAllCityNames returns capitalised names")
    void getAllCityNames_Capitalised() {
        List<String> all = service.getAllCityNames();
        for (String city : all) {
            assertTrue(Character.isUpperCase(city.charAt(0)),
                    "City name should be capitalised: " + city);
        }
    }
}

