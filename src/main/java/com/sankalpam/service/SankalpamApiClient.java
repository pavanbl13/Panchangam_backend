package com.sankalpam.service;

import com.sankalpam.model.Coordinates;
import com.sankalpam.model.SankalpamFinder;

public interface SankalpamApiClient {
    SankalpamFinder fetchSankalpam(String city, Coordinates coords, String timezone, String dateStr, String timeStr);
}