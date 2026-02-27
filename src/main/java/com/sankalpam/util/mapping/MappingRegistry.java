package com.sankalpam.util.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Static Mapping Registry - Contains all hardcoded mapping data
 * Populated once from mappings.xlsx during development
 * No runtime dependency on Excel files
 */
public class MappingRegistry {

    private static final Map<String, MappingSheet> sheets = new HashMap<>();

    static {
        initializeMappings();
    }

    private static void initializeMappings() {
        initializeMaasam();
        initializeRuthu();
        initializeAyanam();
        initializeTithi();
        initializePaksham();
        initializeVaaram();
        initializeNakshatram();
    }

    /**
     * Maasam (Month in Hindu Calendar)
     */
    private static void initializeMaasam() {
        MappingSheet maasam = new MappingSheet("Maasam", "Maasam", "Auto-loaded from Excel");

        maasam.addMapping("Meesam", "Chaitram");
        maasam.addMapping("Vṛuṣabha", "Vaishakam");
        maasam.addMapping("Mithuna", "Jyestam");
        maasam.addMapping("Karkaṭa", "Ashadam");
        maasam.addMapping("Siṃha", "Shravanam");
        maasam.addMapping("Kanya", "Badhrapadam");
        maasam.addMapping("Tula", "Ashwayujam");
        maasam.addMapping("Vruscika", "Karthikam");
        maasam.addMapping("Dhanus", "Maargasiram");
        maasam.addMapping("Makara", "Pushyam");
        maasam.addMapping("Kumbha", "Maagham");
        maasam.addMapping("Mena", "Phalgunam");

        sheets.put("Maasam", maasam);
    }

    /**
     * Ruthu (Season)
     */
    private static void initializeRuthu() {
        MappingSheet ruthu = new MappingSheet("Ruthu", "Ruthu", "Hindu Calendar Season");

        ruthu.addMapping("vasanta", "Vasanta Ruthu");
        ruthu.addMapping("grishma", "Grishma Ruthu");
        ruthu.addMapping("varsha", "Varsha Ruthu");
        ruthu.addMapping("sharad", "Sharad Ruthu");
        ruthu.addMapping("hemanta", "Hemanta Ruthu");
        ruthu.addMapping("shishira", "Shishira Ruthu");
        ruthu.addMapping("sisira", "Shishira Ruthu");

        sheets.put("Ruthu", ruthu);
    }

    /**
     * Ayanam (Solar Half Year)
     */
    private static void initializeAyanam() {
        MappingSheet ayanam = new MappingSheet("Ayanam", "Ayanam", "Solar Half Year");

        ayanam.addMapping("uttarayanam", "Uttarayanam");
        ayanam.addMapping("uttarayana", "Uttarayanam");
        ayanam.addMapping("uttarayan", "Uttarayanam");
        ayanam.addMapping("dakshinayanam", "Dakshinayanam");
        ayanam.addMapping("dakshinayana", "Dakshinayanam");
        ayanam.addMapping("dakshinayan", "Dakshinayanam");

        sheets.put("Ayanam", ayanam);
    }

    /**
     * Tithi (Lunar Day)
     */
    private static void initializeTithi() {
        MappingSheet tithi = new MappingSheet("Tithi", "Tithi", "Lunar Day");

        tithi.addMapping("prathama", "Prathama");
        tithi.addMapping("dwitiya", "Dwitiya");
        tithi.addMapping("tritiya", "Tritiya");
        tithi.addMapping("chaturthi", "Chaturthi");
        tithi.addMapping("panchami", "Panchami");
        tithi.addMapping("shashthi", "Shashthi");
        tithi.addMapping("saptami", "Saptami");
        tithi.addMapping("ashtami", "Ashtami");
        tithi.addMapping("ashtamyam", "Ashtami");
        tithi.addMapping("navami", "Navami");
        tithi.addMapping("dashami", "Dashami");
        tithi.addMapping("ekadashi", "Ekadashi");
        tithi.addMapping("dwadashi", "Dwadashi");
        tithi.addMapping("trayodashi", "Trayodashi");
        tithi.addMapping("chaturdashi", "Chaturdashi");
        tithi.addMapping("purnima", "Purnima");
        tithi.addMapping("amavasya", "Amavasya");

        sheets.put("Tithi", tithi);
    }

    /**
     * Paksham (Lunar Fortnight)
     */
    private static void initializePaksham() {
        MappingSheet paksham = new MappingSheet("Paksham", "Paksham", "Lunar Fortnight");

        paksham.addMapping("shukla", "Shukla Paksham");
        paksham.addMapping("shuklapaksha", "Shukla Paksham");
        paksham.addMapping("krishna", "Krishna Paksham");
        paksham.addMapping("krishnapaksha", "Krishna Paksham");

        sheets.put("Paksham", paksham);
    }

    /**
     * Vaaram (Day of the Week)
     */
    private static void initializeVaaram() {
        MappingSheet vaaram = new MappingSheet("Vaaram", "Vaaram", "Day of the Week");

        vaaram.addMapping("bhanu", "Bhanu Vaaram");
        vaaram.addMapping("soma", "Soma Vaaram");
        vaaram.addMapping("mangal", "Mangal Vaaram");
        vaaram.addMapping("budha", "Budha Vaaram");
        vaaram.addMapping("guru", "Guru Vaaram");
        vaaram.addMapping("shukra", "Shukra Vaaram");
        vaaram.addMapping("shani", "Shani Vaaram");
        vaaram.addMapping("bhouma", "Mangal Vaaram");
        vaaram.addMapping("brihaspati", "Guru Vaaram");
        vaaram.addMapping("shukran", "Shukra Vaaram");

        sheets.put("Vaaram", vaaram);
    }

    /**
     * Nakshatram (Lunar Mansion)
     */
    private static void initializeNakshatram() {
        MappingSheet nakshatram = new MappingSheet("Nakshatram", "Nakshatram", "Lunar Mansion");

        nakshatram.addMapping("ashwini", "Ashwini");
        nakshatram.addMapping("bharani", "Bharani");
        nakshatram.addMapping("krittika", "Krittika");
        nakshatram.addMapping("krithika", "Krittika");
        nakshatram.addMapping("rohini", "Rohini");
        nakshatram.addMapping("mrigashira", "Mrigashira");
        nakshatram.addMapping("ardra", "Ardra");
        nakshatram.addMapping("punarvasu", "Punarvasu");
        nakshatram.addMapping("pushya", "Pushya");
        nakshatram.addMapping("pushyami", "Pushya");
        nakshatram.addMapping("aslesha", "Aslesha");
        nakshatram.addMapping("magha", "Magha");
        nakshatram.addMapping("purva phalguni", "Purva Phalguni");
        nakshatram.addMapping("purva phalguni", "Purva Phalguni");
        nakshatram.addMapping("purva", "Purva Phalguni");
        nakshatram.addMapping("uttara phalguni", "Uttara Phalguni");
        nakshatram.addMapping("uttara", "Uttara Phalguni");
        nakshatram.addMapping("hasta", "Hasta");
        nakshatram.addMapping("chitra", "Chitra");
        nakshatram.addMapping("svati", "Svati");
        nakshatram.addMapping("visakha", "Visakha");
        nakshatram.addMapping("anuradha", "Anuradha");
        nakshatram.addMapping("jyeshtha", "Jyeshtha");
        nakshatram.addMapping("mula", "Mula");
        nakshatram.addMapping("purvashadha", "Purvashadha");
        nakshatram.addMapping("uttarashadha", "Uttarashadha");
        nakshatram.addMapping("shravana", "Shravana");
        nakshatram.addMapping("dhanishtha", "Dhanishtha");
        nakshatram.addMapping("shatabhisha", "Shatabhisha");
        nakshatram.addMapping("purva bhadrapada", "Purva Bhadrapada");
        nakshatram.addMapping("uttara bhadrapada", "Uttara Bhadrapada");
        nakshatram.addMapping("revati", "Revati");

        sheets.put("Nakshatram", nakshatram);
    }

    /**
     * Get a mapping sheet by name
     */
    public static MappingSheet getSheet(String sheetName) {
        return sheets.get(sheetName);
    }

    /**
     * Get mapped value from a specific sheet
     */
    public static String getMappedValue(String sheetName, String key) {
        MappingSheet sheet = sheets.get(sheetName);
        if (sheet == null) {
            return key;
        }
        return sheet.getMappedValue(key);
    }

    /**
     * Check if a sheet exists
     */
    public static boolean hasSheet(String sheetName) {
        return sheets.containsKey(sheetName);
    }

    /**
     * Get all available sheets
     */
    public static Map<String, MappingSheet> getAllSheets() {
        return sheets;
    }

    /**
     * Get count of all mappings across all sheets
     */
    public static int getTotalMappingCount() {
        return sheets.values().stream()
                .mapToInt(MappingSheet::getMappingCount)
                .sum();
    }

    /**
     * Print registry info (for debugging)
     */
    public static void printRegistryInfo() {
        System.out.println("================================================================================");
        System.out.println("MAPPING REGISTRY INFO");
        System.out.println("================================================================================");
        sheets.forEach((name, sheet) -> {
            System.out.println("Sheet: " + name + " | Field: " + sheet.getFieldName() +
                             " | Mappings: " + sheet.getMappingCount());
        });
        System.out.println("Total Mappings: " + getTotalMappingCount());
        System.out.println("================================================================================");
    }
}

