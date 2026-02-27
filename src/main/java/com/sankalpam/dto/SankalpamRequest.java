package com.sankalpam.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Data Transfer Object for Sankalpam form submission.
 * All fields validated via Bean Validation (Hibernate Validator).
 */
@Data
public class SankalpamRequest {

    // Personal Details
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[A-Za-z\\s.'\\-]+$", message = "Name contains invalid characters")
    private String fullName;

    @NotBlank(message = "Gotram is required")
    @Size(min = 2, max = 100, message = "Gotram must be between 2 and 100 characters")
    private String gotram;

    @NotBlank(message = "Nakshatram (birth star) is required")
    private String nakshatram;

    @NotBlank(message = "Rasi is required")
    private String rasi;

    // Traditional Calendar Fields
    @NotBlank(message = "Samvatsaram (year) is required")
    private String samvatsaram;

    @NotBlank(message = "Ayanam is required")
    @Pattern(regexp = "Uttarayanam|Dakshinayanam", message = "Invalid Ayanam value")
    private String ayanam;

    @NotBlank(message = "Ruthu (season) is required")
    private String ruthu;

    @NotBlank(message = "Masam (month) is required")
    private String masam;

    @NotBlank(message = "Paksham is required")
    @Pattern(regexp = "Shukla Paksham|Krishna Paksham", message = "Invalid Paksham value")
    private String paksham;

    @NotBlank(message = "Tithi is required")
    private String tithi;

    @NotBlank(message = "Vaasaram (day) is required")
    private String vaasaram;

    // Location
    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "City/Location is required")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String city;

    @NotBlank(message = "State/Region is required")
    private String state;

    // Sankalpa Purpose
    @NotBlank(message = "Sankalpa purpose is required")
    @Size(min = 5, max = 500, message = "Sankalpa must be between 5 and 500 characters")
    private String sankalpaPurpose;

    @Size(max = 300, message = "Additional notes cannot exceed 300 characters")
    private String additionalNotes;

    // Contact (optional)
    @Email(message = "Invalid email address format")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Pattern(regexp = "^(\\+?[1-9]\\d{1,14})?$", message = "Invalid phone number format")
    private String phone;

    public String getAdditionalNotes() {
        return additionalNotes;
    }
}
