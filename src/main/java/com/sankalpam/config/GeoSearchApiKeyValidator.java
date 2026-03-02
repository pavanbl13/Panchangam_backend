package com.sankalpam.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeoSearchApiKeyValidator {

    private final GeoSearchApiProperties geoSearchApiProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void validateApiKey() {
        String apiKey = geoSearchApiProperties.getKey();

        log.info("================================================================================");
        log.info("GEOSEARCH API KEY VALIDATION");
        log.info("================================================================================");

        if (apiKey == null) {
            log.error("GEOSEARCH_API_KEY environment variable is NULL");
            log.error("================================================================================");
            log.error("CRITICAL: Geoapify API key is required to run this application");
            log.error("================================================================================");
            log.error("Please set the GEOSEARCH_API_KEY environment variable before starting the application:");
            log.error("  Windows CMD: set GEOSEARCH_API_KEY=your_api_key_here");
            log.error("  PowerShell: $env:GEOSEARCH_API_KEY=\"your_api_key_here\"");
            log.error("  Bash: export GEOSEARCH_API_KEY=your_api_key_here");
            log.error("================================================================================");
            throw new IllegalStateException("GEOSEARCH_API_KEY environment variable is not set. Application startup failed.");
        } else if (apiKey.trim().isEmpty()) {
            log.error("GEOSEARCH_API_KEY environment variable is EMPTY STRING");
            log.error("================================================================================");
            log.error("CRITICAL: Geoapify API key is required to run this application");
            log.error("================================================================================");
            log.error("Please set the GEOSEARCH_API_KEY environment variable before starting the application:");
            log.error("  Windows CMD: set GEOSEARCH_API_KEY=your_api_key_here");
            log.error("  PowerShell: $env:GEOSEARCH_API_KEY=\"your_api_key_here\"");
            log.error("  Bash: export GEOSEARCH_API_KEY=your_api_key_here");
            log.error("================================================================================");
            throw new IllegalStateException("GEOSEARCH_API_KEY environment variable is empty. Application startup failed.");
        } else if (apiKey.trim().length() < 10) {
            log.error("GEOSEARCH_API_KEY is TOO SHORT - length: {} characters", apiKey.trim().length());
            log.error("================================================================================");
            log.error("CRITICAL: Geoapify API key must be at least 10 characters");
            log.error("================================================================================");
            log.error("Please provide a valid GEOSEARCH_API_KEY environment variable:");
            log.error("  Windows CMD: set GEOSEARCH_API_KEY=your_valid_api_key");
            log.error("  PowerShell: $env:GEOSEARCH_API_KEY=\"your_valid_api_key\"");
            log.error("  Bash: export GEOSEARCH_API_KEY=your_valid_api_key");
            log.error("================================================================================");
            throw new IllegalStateException("GEOSEARCH_API_KEY is invalid - too short (" + apiKey.trim().length() + " chars). Application startup failed.");
        } else {
            log.info("GEOSEARCH_API_KEY is configured");
            log.info("API Key length: {} characters", apiKey.trim().length());
            log.info("================================================================================");
            log.info("Geoapify API key validation PASSED - Application will proceed");
            log.info("================================================================================");
        }
    }
}

