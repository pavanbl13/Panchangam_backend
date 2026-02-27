package com.sankalpam.controller;

import java.util.List;

/**
 * Static reference data for Panchanga (Hindu calendar) dropdowns.
 * Package-accessible by SankalpamController.
 */
public final class SankalpamData {

    private SankalpamData() {}

    public static final List<String> SAMVATSARAMS = List.of(
        "Prabhava", "Vibhava", "Shukla", "Pramodoota", "Prajotpathi",
        "Aangirasa", "Shrimukha", "Bhava", "Yuva", "Dhaatu",
        "Eeshwara", "Bahudhanya", "Pramaadhi", "Vikrama", "Vrusha",
        "Chitrabhanu", "Subhanu", "Tharana", "Paarthiva", "Vyaya",
        "Sarvajith", "Sarvadhari", "Virodhi", "Vikruthi", "Khara",
        "Nandana", "Vijaya", "Jaya", "Manmatha", "Durmukhi",
        "Hevilambi", "Vilambi", "Vikaari", "Shaarvari", "Plava",
        "Shubhakruthu", "Shobhakruthu", "Krodhi", "Vishvavasu", "Parabhava",
        "Plavanga", "Keelaka", "Saumya", "Saadharana", "Virodhikruthu",
        "Paridhavi", "Pramaadheecha", "Aananda", "Raakshasa", "Anala",
        "Pingala", "Kaala Yuktha", "Siddharthi", "Roudri", "Durmathi",
        "Dundubhi", "Rudhirodgaari", "Rakthaakshi", "Krodhana", "Akshaya"
    );

    public static final List<String> AYANAMS = List.of(
        "Uttarayanam", "Dakshinayanam"
    );

    public static final List<String> RUTHUS = List.of(
        "Vasantha Ruthu", "Greeshma Ruthu", "Varsha Ruthu",
        "Sharath Ruthu", "Hemantha Ruthu", "Shishira Ruthu"
    );

    public static final List<String> MASAMS = List.of(
        "Chaitra", "Vaishakha", "Jyeshtha", "Ashadha",
        "Shravana", "Bhadrapada", "Ashwija", "Karthika",
        "Margasira", "Pushya", "Magha", "Phalguna"
    );

    public static final List<String> PAKSHAMS = List.of(
        "Shukla Paksham", "Krishna Paksham"
    );

    public static final List<String> TITHIS = List.of(
        "Prathama", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashti", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi",
        "Pournami / Amavasya"
    );

    public static final List<String> VAASARAMS = List.of(
        "Bhanu Vaasaram (Sunday)", "Soma Vaasaram (Monday)",
        "Mangala Vaasaram (Tuesday)", "Budha Vaasaram (Wednesday)",
        "Guru Vaasaram (Thursday)", "Shukra Vaasaram (Friday)",
        "Shani Vaasaram (Saturday)"
    );

    public static final List<String> NAKSHATRAMS = List.of(
        "Ashwini", "Bharani", "Krithika", "Rohini", "Mrigashira",
        "Ardra", "Punarvasu", "Pushyami", "Ashlesha", "Magha",
        "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra",
        "Swati", "Vishaka", "Anuradha", "Jyeshtha", "Moola",
        "Purva Ashadha", "Uttara Ashadha", "Shravanam", "Dhanishtha",
        "Shatabhisha", "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    );

    public static final List<String> RASIS = List.of(
        "Mesha (Aries)", "Vrishabha (Taurus)", "Mithuna (Gemini)",
        "Karka (Cancer)", "Simha (Leo)", "Kanya (Virgo)",
        "Tula (Libra)", "Vrischika (Scorpio)", "Dhanus (Sagittarius)",
        "Makara (Capricorn)", "Kumbha (Aquarius)", "Meena (Pisces)"
    );
}
