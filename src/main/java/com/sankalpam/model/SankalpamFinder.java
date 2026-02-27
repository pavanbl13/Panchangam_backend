package com.sankalpam.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for Sankalpam Finder results.
 * Contains the calculated/found Sankalpam information based on date, time, and city.
 */
public class SankalpamFinder {

    private String id;
    private String date;
    private String time;
    private String city;

    // Panchanga information (to be calculated)
    private String samvatsaram;
    private String ayanam;
    private String ruthu;
    private String masam;
    private String paksham;
    private String tithi;
    private String vaasaram;
    private String nakshatram;
    private String rasi;

    // Sunrise and Sunset times
    private String sunrise;
    private String sunset;
    private String validUntil;

    private Instant createdAt;

    // Constructors
    public SankalpamFinder() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public SankalpamFinder(String date, String time, String city) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.time = time;
        this.city = city;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getSamvatsaram() {
        return samvatsaram;
    }

    public void setSamvatsaram(String samvatsaram) {
        this.samvatsaram = samvatsaram;
    }

    public String getAyanam() {
        return ayanam;
    }

    public void setAyanam(String ayanam) {
        this.ayanam = ayanam;
    }

    public String getRuthu() {
        return ruthu;
    }

    public void setRuthu(String ruthu) {
        this.ruthu = ruthu;
    }

    public String getMasam() {
        return masam;
    }

    public void setMasam(String masam) {
        this.masam = masam;
    }

    public String getPaksham() {
        return paksham;
    }

    public void setPaksham(String paksham) {
        this.paksham = paksham;
    }

    public String getTithi() {
        return tithi;
    }

    public void setTithi(String tithi) {
        this.tithi = tithi;
    }

    public String getVaasaram() {
        return vaasaram;
    }

    public void setVaasaram(String vaasaram) {
        this.vaasaram = vaasaram;
    }

    public String getNakshatram() {
        return nakshatram;
    }

    public void setNakshatram(String nakshatram) {
        this.nakshatram = nakshatram;
    }

    public String getRasi() {
        return rasi;
    }

    public void setRasi(String rasi) {
        this.rasi = rasi;
    }

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SankalpamFinder{" +
                "id='" + id + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", city='" + city + '\'' +
                ", samvatsaram='" + samvatsaram + '\'' +
                ", ayanam='" + ayanam + '\'' +
                ", ruthu='" + ruthu + '\'' +
                ", masam='" + masam + '\'' +
                ", paksham='" + paksham + '\'' +
                ", tithi='" + tithi + '\'' +
                ", vaasaram='" + vaasaram + '\'' +
                ", nakshatram='" + nakshatram + '\'' +
                ", rasi='" + rasi + '\'' +
                '}';
    }
}
