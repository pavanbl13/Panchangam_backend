package com.sankalpam.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing a submitted Sankalpam.
 * In production this would be a JPA @Entity.
 */
@Data
@Builder
public class Sankalpam {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String fullName;
    private String gotram;
    private String nakshatram;
    private String rasi;

    private String samvatsaram;
    private String ayanam;
    private String ruthu;
    private String masam;
    private String paksham;
    private String tithi;
    private String vaasaram;

    private String country;
    private String city;
    private String state;

    private String sankalpaPurpose;
    private String additionalNotes;

    private String email;
    private String phone;

    @Builder.Default
    private Instant submittedAt = Instant.now();
}
