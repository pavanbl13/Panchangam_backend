package com.sankalpam.service.mapping;

/**
 * Interface for mapping API response values to display-friendly values
 * Uses JSON-based lookup from src/main/resources/lookup/
 * For attributes with JSON mappings, returns transformed value
 * For attributes without JSON mappings, returns original value unchanged
 */
public interface MappingService {

    /**
     * Map a value using JSON lookup file if available
     * If JSON file exists for the attributeName, searches for the value and returns mapped result
     * If no JSON file exists, returns the original value unchanged
     *
     * @param attributeName the attribute name (e.g., "Maasam", "Ruthu")
     * @param value the API response value to map
     * @return mapped value from JSON if found, otherwise original value
     */
    String mapValue(String attributeName, String value);

    /**
     * Map Maasam (Month) - has JSON mapping available
     */
    String mapMaasam(String value);

    /**
     * Map Ruthu (Season) - no JSON mapping, passes through unchanged
     */
    String mapRuthu(String value);

    /**
     * Map Ayanam (Solar Half Year) - no JSON mapping, passes through unchanged
     */
    String mapAyanam(String value);

    /**
     * Map Tithi (Lunar Day) - no JSON mapping, passes through unchanged
     */
    String mapTithi(String value);

    /**
     * Map Paksham (Lunar Fortnight) - no JSON mapping, passes through unchanged
     */
    String mapPaksham(String value);

    /**
     * Map Vaaram (Day of the Week) - no JSON mapping, passes through unchanged
     */
    String mapVaaram(String value);

    /**
     * Map Vaasare (day of week) - maps day names to Vaasare values using Vaasare.json
     */
    String mapVaasare(String dayOfWeek);

    /**
     * Map Nakshatram (Lunar Mansion) - no JSON mapping, passes through unchanged
     */
    String mapNakshatram(String value);

    /**
     * Check if a JSON mapping file exists for the attribute
     */
    boolean hasMapping(String attributeName);

    /**
     * Get all Maasam mappings including date ranges and month information
     * Returns a map where key is Maasam name and value contains range and months data
     */
    java.util.Map<String, java.util.Map<String, Object>> getAllMaasamMappings();

    /**
     * Get all Ruthuvu mappings including associated Maasams
     * Returns a map where key is Ruthuvu name and value contains maasam list
     */
    java.util.Map<String, java.util.Map<String, Object>> getAllRuthuMappings();
}

