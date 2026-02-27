package com.sankalpam.util.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single mapping sheet (category) from the mappings.xlsx file.
 * Contains a map of key-value pairs for looking up display values.
 */
public class MappingSheet {
    private String sheetName;
    private String fieldName;
    private String description;
    private Map<String, MappingEntry> mappings;

    public MappingSheet(String sheetName, String fieldName) {
        this.sheetName = sheetName;
        this.fieldName = fieldName;
        this.mappings = new HashMap<>();
    }

    public MappingSheet(String sheetName, String fieldName, String description) {
        this.sheetName = sheetName;
        this.fieldName = fieldName;
        this.description = description;
        this.mappings = new HashMap<>();
    }

    public void addMapping(String key, String displayValue) {
        this.mappings.put(key.toLowerCase().trim(), new MappingEntry(key, displayValue));
    }

    public void addMapping(String key, String displayValue, String description) {
        this.mappings.put(key.toLowerCase().trim(), new MappingEntry(key, displayValue, description));
    }

    public void addMappingEntry(MappingEntry entry) {
        this.mappings.put(entry.getKey().toLowerCase().trim(), entry);
    }

    public String getMappedValue(String key) {
        if (key == null) {
            return key;
        }
        MappingEntry entry = this.mappings.get(key.toLowerCase().trim());
        return entry != null ? entry.getDisplayValue() : key;
    }

    public MappingEntry getMappingEntry(String key) {
        return this.mappings.get(key.toLowerCase().trim());
    }

    public boolean containsKey(String key) {
        return this.mappings.containsKey(key.toLowerCase().trim());
    }

    public int getMappingCount() {
        return this.mappings.size();
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, MappingEntry> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, MappingEntry> mappings) {
        this.mappings = mappings;
    }

    @Override
    public String toString() {
        return "MappingSheet{" +
                "sheetName='" + sheetName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", description='" + description + '\'' +
                ", mappingCount=" + mappings.size() +
                '}';
    }
}
