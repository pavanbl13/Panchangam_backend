package com.sankalpam.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GeoSearchApiKeyValidator Tests")
class GeoSearchApiKeyValidatorTest {

    @Test
    @DisplayName("Should throw when API key is null")
    void validateApiKey_NullKey_ThrowsException() {
        GeoSearchApiProperties props = new GeoSearchApiProperties();
        props.setKey(null);
        GeoSearchApiKeyValidator validator = new GeoSearchApiKeyValidator(props);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                validator::validateApiKey);
        assertTrue(ex.getMessage().contains("not set"));
    }

    @Test
    @DisplayName("Should throw when API key is empty")
    void validateApiKey_EmptyKey_ThrowsException() {
        GeoSearchApiProperties props = new GeoSearchApiProperties();
        props.setKey("   ");
        GeoSearchApiKeyValidator validator = new GeoSearchApiKeyValidator(props);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                validator::validateApiKey);
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("Should throw when API key is too short")
    void validateApiKey_ShortKey_ThrowsException() {
        GeoSearchApiProperties props = new GeoSearchApiProperties();
        props.setKey("abc");
        GeoSearchApiKeyValidator validator = new GeoSearchApiKeyValidator(props);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                validator::validateApiKey);
        assertTrue(ex.getMessage().contains("too short"));
    }

    @Test
    @DisplayName("Should pass when API key is valid")
    void validateApiKey_ValidKey_NoException() {
        GeoSearchApiProperties props = new GeoSearchApiProperties();
        props.setKey("valid-api-key-1234567890");
        GeoSearchApiKeyValidator validator = new GeoSearchApiKeyValidator(props);

        assertDoesNotThrow(validator::validateApiKey);
    }
}

