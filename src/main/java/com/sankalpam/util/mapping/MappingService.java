package com.sankalpam.util.mapping;

import org.springframework.stereotype.Service;

/**
 * Service for mapping API response values to display-friendly values
 * Uses the static MappingRegistry for all lookups
 */
@Service
public class MappingService {

    /**
     * Map a value using the specified sheet
     */
    public String mapValue(String sheetName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String mappedValue = MappingRegistry.getMappedValue(sheetName, value);
        return mappedValue;
    }

    /**
     * Map Maasam (Month)
     */
    public String mapMaasam(String value) {
        return mapValue("Maasam", value);
    }

    /**
     * Map Ruthu (Season)
     */
    public String mapRuthu(String value) {
        return mapValue("Ruthu", value);
    }

    /**
     * Map Ayanam (Solar Half Year)
     */
    public String mapAyanam(String value) {
        return mapValue("Ayanam", value);
    }

    /**
     * Map Tithi (Lunar Day)
     */
    public String mapTithi(String value) {
        return mapValue("Tithi", value);
    }

    /**
     * Map Paksham (Lunar Fortnight)
     */
    public String mapPaksham(String value) {
        return mapValue("Paksham", value);
    }

    /**
     * Map Vaaram (Day of the Week)
     */
    public String mapVaaram(String value) {
        return mapValue("Vaaram", value);
    }

    /**
     * Map Nakshatram (Lunar Mansion)
     */
    public String mapNakshatram(String value) {
        return mapValue("Nakshatram", value);
    }

    /**
     * Check if a mapping sheet exists
     */
    public boolean hasSheet(String sheetName) {
        return MappingRegistry.hasSheet(sheetName);
    }

    /**
     * Print registry information for debugging
     */
    public void printMappingInfo() {
        MappingRegistry.printRegistryInfo();
    }
}



