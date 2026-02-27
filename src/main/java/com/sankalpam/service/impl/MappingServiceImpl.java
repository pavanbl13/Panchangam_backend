package com.sankalpam.service.impl;

import com.sankalpam.service.mapping.JsonMappingLoader;
import com.sankalpam.service.mapping.MappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of MappingService using JSON-based lookups
 * Checks JSON files first; if mapping exists, returns transformed value
 * If no JSON mapping exists, returns original value unchanged
 */
@Slf4j
@Service
public class MappingServiceImpl implements MappingService {

    @Override
    public String mapValue(String attributeName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String mappedValue = JsonMappingLoader.getMappedValue(attributeName, value);

        if (mappedValue != null) {
            log.debug("Mapping {}: {} -> {}", attributeName, value, mappedValue);
            return mappedValue;
        }

        log.debug("No mapping found for {} in {}, returning original value", value, attributeName);
        return value;
    }

    @Override
    public String mapMaasam(String value) {
        return mapValue("Maasam", value);
    }

    @Override
    public String mapRuthu(String value) {
        return value;
    }

    @Override
    public String mapAyanam(String value) {
        return value;
    }

    @Override
    public String mapTithi(String value) {
        return value;
    }

    @Override
    public String mapPaksham(String value) {
        return value;
    }

    @Override
    public String mapVaaram(String value) {
        return value;
    }

    @Override
    public String mapVaasare(String dayOfWeek) {
        return mapValue("Vaasare", dayOfWeek);
    }

    @Override
    public String mapNakshatram(String value) {
        return value;
    }

    @Override
    public boolean hasMapping(String attributeName) {
        return JsonMappingLoader.hasMapping(attributeName);
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Object>> getAllMaasamMappings() {
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> maasamMap = JsonMappingLoader.getAllMappings().get("Maasam");
        if (maasamMap == null) {
            return new java.util.HashMap<>();
        }
        // Cast the result to the expected type
        return (java.util.Map<String, java.util.Map<String, Object>>) (java.util.Map<?, ?>) maasamMap;
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Object>> getAllRuthuMappings() {
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> ruthuMap = JsonMappingLoader.getAllMappings().get("Ruthuvu");
        if (ruthuMap == null) {
            return new java.util.HashMap<>();
        }
        // Cast the result to the expected type
        return (java.util.Map<String, java.util.Map<String, Object>>) (java.util.Map<?, ?>) ruthuMap;
    }
}

