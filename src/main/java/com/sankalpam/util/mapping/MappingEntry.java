package com.sankalpam.util.mapping;

/**
 * Represents a single mapping entry from API response value to user-friendly display value.
 * Each entry has a key (API value) and a display value (what users see).
 */
public class MappingEntry {
    private String key;
    private String displayValue;
    private String description;

    public MappingEntry(String key, String displayValue) {
        this.key = key;
        this.displayValue = displayValue;
    }

    public MappingEntry(String key, String displayValue, String description) {
        this.key = key;
        this.displayValue = displayValue;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "MappingEntry{" +
                "key='" + key + '\'' +
                ", displayValue='" + displayValue + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
