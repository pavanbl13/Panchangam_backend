package com.sankalpam.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CityGeoInfo {

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("countryCode")
    private String countryCode;

    public CityGeoInfo(double latitude, double longitude, String timezone) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
    }

    public CityGeoInfo(double latitude, double longitude, String timezone, String countryCode) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
        this.countryCode = countryCode;
    }

    public Coordinates toCoordinates() {
        return new Coordinates(latitude, longitude);
    }
}
