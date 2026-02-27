package com.sankalpam.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simplified Panchanga response payload for the /find endpoint.
 */
public class SankalpamPanchangaResponse {
    @JsonProperty("samvatsaram")
    private String Samvatsaram;
    @JsonProperty("ayanam")
    private String Ayanam;
    @JsonProperty("ruthuvu")
    private String Ruthuvu;
    @JsonProperty("maasam")
    private String Maasam;
    @JsonProperty("paksham")
    private String Paksham;
    @JsonProperty("tithi")
    private String Tithi;
    @JsonProperty("vaaram")
    private String Vaaram;
    @JsonProperty("nakshatram")
    private String Nakshatram;
    @JsonProperty("sunrise")
    private String Sunrise;
    @JsonProperty("sunset")
    private String Sunset;
    @JsonProperty("validUntil")
    private String ValidUntil;

    public String getSamvatsaram() {
        return Samvatsaram;
    }

    public void setSamvatsaram(String samvatsaram) {
        this.Samvatsaram = samvatsaram;
    }

    public String getAyanam() {
        return Ayanam;
    }

    public void setAyanam(String ayanam) {
        this.Ayanam = ayanam;
    }

    public String getRuthuvu() {
        return Ruthuvu;
    }

    public void setRuthuvu(String ruthuvu) {
        this.Ruthuvu = ruthuvu;
    }

    public String getMaasam() {
        return Maasam;
    }

    public void setMaasam(String maasam) {
        this.Maasam = maasam;
    }

    public String getPaksham() {
        return Paksham;
    }

    public void setPaksham(String paksham) {
        this.Paksham = paksham;
    }

    public String getTithi() {
        return Tithi;
    }

    public void setTithi(String tithi) {
        this.Tithi = tithi;
    }

    public String getVaaram() {
        return Vaaram;
    }

    public void setVaaram(String vaaram) {
        this.Vaaram = vaaram;
    }

    public String getNakshatram() {
        return Nakshatram;
    }

    public void setNakshatram(String nakshatram) {
        this.Nakshatram = nakshatram;
    }

    public String getSunrise() {
        return Sunrise;
    }

    public void setSunrise(String sunrise) {
        this.Sunrise = sunrise;
    }

    public String getSunset() {
        return Sunset;
    }

    public void setSunset(String sunset) {
        this.Sunset = sunset;
    }

    public String getValidUntil() {
        return ValidUntil;
    }

    public void setValidUntil(String validUntil) {
        this.ValidUntil = validUntil;
    }
}

