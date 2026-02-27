package com.sankalpam.dto;

import jakarta.validation.constraints.*;

/**
 * Data Transfer Object for Sankalpam Finder form.
 * Simplified request with only essential fields for finding Sankalpam date/time.
 */
public class SankalpamFinderRequest {

    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in YYYY-MM-DD format")
    private String date;

    @NotBlank(message = "Time is required")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Time must be in HH:MM format (24-hour)")
    private String time;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String city;

    // Constructors
    public SankalpamFinderRequest() {
    }

    public SankalpamFinderRequest(String date, String time, String city) {
        this.date = date;
        this.time = time;
        this.city = city;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "SankalpamFinderRequest{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
