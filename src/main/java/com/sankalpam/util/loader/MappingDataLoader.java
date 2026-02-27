package com.sankalpam.util.loader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Utility to load mapping data from Excel file and generate Java code
 *
 * This is a ONE-TIME development tool to:
 * 1. Read mappings.xlsx file (can contain 1 or more sheets)
 * 2. Extract mapping data from each sheet
 * 3. Generate Java code snippets
 * 4. Print to console AND write to file
 *
 * SUPPORTS:
 * - Single sheet: Only loads that sheet, doesn't affect other categories
 * - Multiple sheets: Loads all sheets
 *
 * USAGE:
 * 1. Place mappings.xlsx in src/main/resources/data/ folder
 * 2. Run: mvn exec:java@run-loader
 * 3. Copy generated code from GENERATED_MAPPINGS.txt file
 * 4. Find the matching method in MappingRegistry.java and replace it
 * 5. Delete Excel file - no longer needed
 * 6. Compile: mvn clean compile
 */
public class MappingDataLoader {

    private static final String EXCEL_FILE_PATH = "src/main/resources/data/mappings.xlsx";
    private static final String OUTPUT_FILE = "GENERATED_MAPPINGS.txt";

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("MAPPING DATA LOADER - Excel to Java Code Generator");
        System.out.println("================================================================================");
        System.out.println();

        try {
            loadAndGenerateCode();
        } catch (IOException e) {
            System.err.println("Error loading Excel file: " + e.getMessage());
            e.printStackTrace();
            System.out.println();
            System.out.println("TROUBLESHOOTING:");
            System.out.println("1. Ensure mappings.xlsx exists at: " + EXCEL_FILE_PATH);
            System.out.println("2. Excel file should have columns: Key | DisplayValue | Description");
            System.out.println("3. Each sheet name becomes a mapping category");
            System.out.println("4. You can have 1 or more sheets in the Excel file");
        }
    }

    private static void loadAndGenerateCode() throws IOException {
        // Try multiple possible locations for the Excel file
        File excelFile = null;
        String[] possiblePaths = {
            "src/main/resources/data/mappings.xlsx",                                    // Relative from backend
            "./src/main/resources/data/mappings.xlsx",                                  // From current directory
            System.getProperty("user.dir") + "/src/main/resources/data/mappings.xlsx"  // From working directory
        };

        System.out.println("Searching for mappings.xlsx...");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        System.out.println();

        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists()) {
                excelFile = f;
                System.out.println("Found: " + f.getAbsolutePath());
                System.out.println();
                break;
            }
        }

        if (excelFile == null || !excelFile.exists()) {
            System.err.println("ERROR: Could not find mappings.xlsx!");
            System.err.println();
            System.err.println("Searched in:");
            for (String path : possiblePaths) {
                System.err.println("  - " + new File(path).getAbsolutePath());
            }
            System.err.println();
            throw new IOException("File not found: mappings.xlsx not found in any of the expected locations");
        }

        StringBuilder generatedCode = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            int sheetCount = workbook.getNumberOfSheets();
            System.out.println("Found " + sheetCount + " sheet(s)");
            System.out.println();

            if (sheetCount == 1) {
                System.out.println("NOTE: You have 1 sheet in your Excel file.");
                System.out.println("Only that sheet's data will be generated.");
                System.out.println("Existing mappings in MappingRegistry will NOT be affected.");
                System.out.println();
            } else {
                System.out.println("NOTE: You have " + sheetCount + " sheets in your Excel file.");
                System.out.println("Only these sheets' data will be generated.");
                System.out.println("Other existing mappings in MappingRegistry will NOT be affected.");
                System.out.println();
            }

            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                System.out.println("Processing sheet: " + sheetName);
                String code = generateCodeForSheet(sheet, sheetName);
                generatedCode.append(code);
                System.out.println();
            }

            System.out.println("================================================================================");
            System.out.println("CODE GENERATION COMPLETE!");
            System.out.println("================================================================================");
            System.out.println();
            System.out.println("GENERATED CODE WRITTEN TO: " + OUTPUT_FILE);
            System.out.println();
            System.out.println("NEXT STEPS:");
            System.out.println("1. Open file: " + OUTPUT_FILE);
            System.out.println("2. Copy the generated code");
            System.out.println("3. Find the matching method in MappingRegistry.java:");
            System.out.println("   private static void initialize<SheetName>() { ... }");
            System.out.println("4. Replace ONLY that method with the new generated code");
            System.out.println("5. Keep all other initialize*() methods unchanged");
            System.out.println("6. Verify the method call exists in initializeMappings()");
            System.out.println("7. Delete mappings.xlsx - it's no longer needed");
            System.out.println("8. Run: mvn clean compile");
            System.out.println();
            System.out.println("IMPORTANT:");
            System.out.println("You ONLY replace the initialize method for the sheet(s) you're updating");
            System.out.println("All other categories remain unchanged in MappingRegistry");
            System.out.println();

            // Write to file
            writeToFile(generatedCode.toString());
            System.out.println("File saved: " + new File(OUTPUT_FILE).getAbsolutePath());
            System.out.println();
        }
    }

    private static String generateCodeForSheet(Sheet sheet, String sheetName) {
        List<MappingData> mappings = extractMappings(sheet);
        StringBuilder code = new StringBuilder();

        if (mappings.isEmpty()) {
            System.out.println("  WARNING: No mapping data found in sheet " + sheetName);
            return code.toString();
        }

        code.append("\n");
        code.append("    /**\n");
        code.append("     * ").append(sheetName).append("\n");
        code.append("     */\n");
        code.append("    private static void initialize").append(toCamelCase(sheetName)).append("() {\n");
        code.append("        MappingSheet ").append(toLowerCamelCase(sheetName)).append(" = new MappingSheet(\"").append(sheetName).append("\", \"").append(sheetName).append("\", \"Auto-loaded from Excel\");\n");
        code.append("\n");

        for (MappingData mapping : mappings) {
            String key = sanitizeString(mapping.key);
            String displayValue = sanitizeString(mapping.displayValue);
            String description = mapping.description != null ? sanitizeString(mapping.description) : "";

            code.append("        ").append(toLowerCamelCase(sheetName)).append(".addMapping(\"").append(key).append("\", \"").append(displayValue).append("\");\n");
        }

        code.append("\n");
        code.append("        sheets.put(\"").append(sheetName).append("\", ").append(toLowerCamelCase(sheetName)).append(");\n");
        code.append("    }\n");
        code.append("\n");
        code.append("    Total entries in ").append(sheetName).append(": ").append(mappings.size()).append("\n");

        System.out.println(code.toString());
        return code.toString();
    }

    private static List<MappingData> extractMappings(Sheet sheet) {
        List<MappingData> mappings = new ArrayList<>();
        Iterator<Row> rowIterator = sheet.iterator();

        boolean isHeader = true;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            if (isHeader) {
                isHeader = false;
                continue;
            }

            String key = getCellValue(row.getCell(0));
            String displayValue = getCellValue(row.getCell(1));
            String description = getCellValue(row.getCell(2));

            if (key != null && !key.trim().isEmpty() && displayValue != null && !displayValue.trim().isEmpty()) {
                mappings.add(new MappingData(key.trim(), displayValue.trim(), description));
            }
        }

        return mappings;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private static String sanitizeString(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\"", "\\\\\"").trim();
    }

    private static String toCamelCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private static String toLowerCamelCase(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1).toLowerCase();
    }

    private static void writeToFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(OUTPUT_FILE)) {
            writer.write("================================================================================\n");
            writer.write("GENERATED MAPPING CODE\n");
            writer.write("================================================================================\n");
            writer.write("\n");
            writer.write("Instructions:\n");
            writer.write("1. Copy the code below\n");
            writer.write("2. Find the matching initialize*() method in MappingRegistry.java\n");
            writer.write("3. Replace ONLY that method (keep all other methods unchanged)\n");
            writer.write("4. Compile: mvn clean compile\n");
            writer.write("\n");
            writer.write("================================================================================\n");
            writer.write(content);
            writer.write("\n");
            writer.write("================================================================================\n");
        }
    }

    static class MappingData {
        String key;
        String displayValue;
        String description;

        MappingData(String key, String displayValue, String description) {
            this.key = key;
            this.displayValue = displayValue;
            this.description = description;
        }
    }
}



