package com.sankalpam.service.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads JSON lookup files from src/main/resources/lookup/ at application startup
 * Each JSON file name (e.g., Maasam.json) becomes an attribute mapping category
 * Supports both simple mappings {"key": "value"} and complex mappings with nested objects
 * Handles both JAR and file system deployments
 */
@Slf4j
@Component
public class JsonMappingLoader {

    private static final Map<String, Map<String, Object>> cachedMappings = new HashMap<>();
    private static final String LOOKUP_PATTERN = "classpath:lookup/*.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ResourcePatternResolver resourcePatternResolver;

    public JsonMappingLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
        loadAllMappings();
    }

    /**
     * Load all JSON files from the lookup directory
     * Works with both JAR and file system paths
     */
    private void loadAllMappings() {
        try {
            Resource[] resources = resourcePatternResolver.getResources(LOOKUP_PATTERN);

            if (resources == null || resources.length == 0) {
                log.warn("No mapping files found at: {}", LOOKUP_PATTERN);
                return;
            }

            for (Resource resource : resources) {
                try {
                    if (resource.exists() && resource.getFilename() != null && resource.getFilename().endsWith(".json")) {
                        loadMappingFromResource(resource);
                    }
                } catch (Exception e) {
                    log.error("Error loading mapping from resource: {}", resource.getFilename(), e);
                }
            }

            log.info("Mapping Loader: Loaded {} attribute mappings", cachedMappings.size());
            cachedMappings.forEach((key, value) ->
                    log.info("Mapping Loader: {} - {} entries loaded", key, value.size())
            );

        } catch (Exception e) {
            log.error("Error loading mappings from lookup directory", e);
        }
    }

    /**
     * Load a single JSON file from Spring Resource and cache it
     * Works with both file system and JAR resources
     */
    private void loadMappingFromResource(Resource resource) {
        try {
            String attributeName = resource.getFilename().replace(".json", "");
            @SuppressWarnings("unchecked")
            Map<String, Object> mappings = objectMapper.readValue(resource.getInputStream(), Map.class);

            cachedMappings.put(attributeName, mappings);
            log.debug("Loaded mapping file: {} with {} entries", attributeName, mappings.size());

        } catch (Exception e) {
            log.error("Error loading mapping file: {}", resource.getFilename(), e);
        }
    }

    /**
     * Get mapped value for an attribute
     * Case-insensitive lookup to handle both uppercase and lowercase API responses
     *
     * @param attributeName the attribute name (e.g., "Maasam")
     * @param value the key to lookup (case-insensitive)
     * @return mapped value if found, null if attribute or key not found
     */
    public static String getMappedValue(String attributeName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        Map<String, Object> mappings = cachedMappings.get(attributeName);
        if (mappings == null) {
            return null;
        }

        // Try exact match first for performance
        Object result = mappings.get(value);
        if (result != null) {
            // If it's a simple string value, return it
            if (result instanceof String) {
                return (String) result;
            }
            // If it's a complex object (like Maasam with range/months), just return the key
            return value;
        }

        // If no exact match, try case-insensitive lookup
        String lowerValue = value.toLowerCase();
        for (Map.Entry<String, Object> entry : mappings.entrySet()) {
            if (entry.getKey().toLowerCase().equals(lowerValue)) {
                Object entryValue = entry.getValue();
                if (entryValue instanceof String) {
                    return (String) entryValue;
                }
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Check if a mapping category exists
     */
    public static boolean hasMapping(String attributeName) {
        return cachedMappings.containsKey(attributeName);
    }

    /**
     * Get all cached mapping categories
     */
    public static Map<String, Map<String, Object>> getAllMappings() {
        return new HashMap<>(cachedMappings);
    }

    /**
     * Print loaded mappings info for debugging
     */
    public static void printMappingInfo() {
        System.out.println("================================================================================");
        System.out.println("JSON MAPPING LOADER INFO");
        System.out.println("================================================================================");
        cachedMappings.forEach((name, mappings) -> {
            System.out.println("Attribute: " + name + " | Mappings: " + mappings.size());
        });
        System.out.println("Total Attributes: " + cachedMappings.size());
        System.out.println("================================================================================");
    }
}

