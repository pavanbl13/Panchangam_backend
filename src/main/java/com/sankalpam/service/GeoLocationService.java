package com.sankalpam.service;

import com.sankalpam.model.Coordinates;

public interface GeoLocationService {
    Coordinates getCoordinates(String city);
}