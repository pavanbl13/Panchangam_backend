package com.sankalpam.service;

import com.sankalpam.dto.SankalpamRequest;
import com.sankalpam.dto.SankalpamFinderRequest;
import com.sankalpam.model.Sankalpam;
import com.sankalpam.model.SankalpamFinder;

/**
 * Service interface for Sankalpam business logic.
 * Defines contracts for Sankalpam operations.
 */
public interface SankalpamService {

    /**
     * Submit a complete Sankalpam request with all traditional calendar fields.
     * 
     * @param request the Sankalpam request containing all required fields
     * @return the processed Sankalpam
     */
    Sankalpam submit(SankalpamRequest request);

    /**
     * Find Sankalpam for a given date, time, and city.
     * Returns calculated Panchanga information.
     * 
     * @param request the Sankalpam Finder request with date, time, and city
     * @return the Panchanga information for the given parameters
     */
    SankalpamFinder findSankalpam(SankalpamFinderRequest request);

}

