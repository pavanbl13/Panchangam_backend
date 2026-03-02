package com.sankalpam.service;

import com.sankalpam.model.CityGeoInfo;

import java.util.Optional;

/**
 * Service for looking up city geo information (latitude, longitude, timezone)
 * from a static JSON-based cache. Avoids redundant API calls for known cities.
 */
public interface CityLookupService {

    /**
     * Look up a city's geo info from the cache.
     * @param city the city name (case-insensitive)
     * @return Optional containing CityGeoInfo if found, empty otherwise
     */
    Optional<CityGeoInfo> lookup(String city);

    /**
     * Add or update a city entry in the cache.
     * Persists the updated data to the JSON file for future use.
     * @param city the city name
     * @param geoInfo the geo info to cache
     */
    void addCity(String city, CityGeoInfo geoInfo);

    /**
     * Check if a city exists in the cache.
     * @param city the city name (case-insensitive)
     * @return true if the city is in the cache
     */
    boolean exists(String city);
}

