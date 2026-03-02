package com.sankalpam.service;

import com.sankalpam.model.CityGeoInfo;
import com.sankalpam.model.Coordinates;

public interface GeoLocationService {
    Coordinates getCoordinates(String city);

    /**
     * Get full geo info (lat, lng, timezone) for a city.
     * First checks the static city lookup cache, then calls Geoapify API if not found.
     * @param city the city name
     * @return CityGeoInfo containing latitude, longitude, and timezone
     */
    CityGeoInfo getGeoInfo(String city);
}