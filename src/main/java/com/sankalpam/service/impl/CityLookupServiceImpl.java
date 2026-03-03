package com.sankalpam.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sankalpam.model.CityGeoInfo;
import com.sankalpam.service.CityLookupService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CityLookupServiceImpl implements CityLookupService {

    private static final String LOOKUP_FILE = "lookup/cities.json";

    private final ConcurrentHashMap<String, CityGeoInfo> cityCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final ReentrantReadWriteLock fileLock = new ReentrantReadWriteLock();

    @PostConstruct
    public void init() {
        loadFromJson();
    }

    private void loadFromJson() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(LOOKUP_FILE)) {
            if (is == null) {
                log.warn("City lookup file not found at classpath: {}. Starting with empty cache.", LOOKUP_FILE);
                return;
            }
            Map<String, CityGeoInfo> data = objectMapper.readValue(is, new TypeReference<Map<String, CityGeoInfo>>() {});
            cityCache.putAll(data);
            log.info("Loaded {} cities from lookup cache: {}", cityCache.size(), cityCache.keySet());
        } catch (IOException e) {
            log.error("Failed to load city lookup file: {}", e.getMessage(), e);
        }
    }

    @Override
    public Optional<CityGeoInfo> lookup(String city) {
        if (city == null || city.trim().isEmpty()) {
            return Optional.empty();
        }
        String key = city.toLowerCase().trim();
        CityGeoInfo info = cityCache.get(key);
        if (info != null) {
            log.info("City lookup HIT for '{}': lat={}, lng={}, tz={}", city, info.getLatitude(), info.getLongitude(), info.getTimezone());
        } else {
            log.info("City lookup MISS for '{}'", city);
        }
        return Optional.ofNullable(info);
    }

    @Override
    public void addCity(String city, CityGeoInfo geoInfo) {
        if (city == null || city.trim().isEmpty() || geoInfo == null) {
            return;
        }
        String key = city.toLowerCase().trim();
        cityCache.put(key, geoInfo);
        log.info("Added city to lookup cache: '{}' -> lat={}, lng={}, tz={}", key, geoInfo.getLatitude(), geoInfo.getLongitude(), geoInfo.getTimezone());
        persistToJson();
    }

    @Override
    public boolean exists(String city) {
        if (city == null || city.trim().isEmpty()) {
            return false;
        }
        return cityCache.containsKey(city.toLowerCase().trim());
    }

    @Override
    public List<String> searchByPrefix(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        String lowerQuery = query.toLowerCase().trim();

        // Match any city whose key contains the query (wildcard / substring match)
        List<String> matches = cityCache.keySet().stream()
                .filter(key -> key.contains(lowerQuery))
                .map(this::capitalise)
                .sorted()
                .collect(Collectors.toList());

        log.info("Local city search for '{}': {} match(es) -> {}", query, matches.size(), matches);
        return matches;
    }

    @Override
    public List<String> getAllCityNames() {
        List<String> all = cityCache.keySet().stream()
                .map(this::capitalise)
                .sorted()
                .collect(Collectors.toList());
        log.info("Returning all {} cached city names", all.size());
        return all;
    }

    /** Capitalise each word: "new york" → "New York" */
    private String capitalise(String name) {
        if (name == null || name.isEmpty()) return name;
        String[] words = name.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }

    private void persistToJson() {
        fileLock.writeLock().lock();
        try {
            // Try to write to the source file if running locally
            Path sourcePath = Paths.get("src", "main", "resources", "lookup", "cities.json");
            if (Files.exists(sourcePath.getParent())) {
                // Sort the map for consistent output
                Map<String, CityGeoInfo> sorted = new LinkedHashMap<>();
                cityCache.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(e -> sorted.put(e.getKey(), e.getValue()));
                objectMapper.writeValue(sourcePath.toFile(), sorted);
                log.info("Persisted {} cities to lookup file: {}", sorted.size(), sourcePath);
            } else {
                log.debug("Source lookup path not found. Cache updated in-memory only.");
            }
        } catch (IOException e) {
            log.error("Failed to persist city lookup to file: {}", e.getMessage(), e);
        } finally {
            fileLock.writeLock().unlock();
        }
    }
}

