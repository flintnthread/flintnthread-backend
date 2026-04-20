package com.ecommerce.authdemo.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SizeColorMapper {

    // Size ID to Name mappings
    private static final Map<String, String> SIZE_MAP = new HashMap<>();
    
    // Color ID to Name mappings  
    private static final Map<String, String> COLOR_MAP = new HashMap<>();

    static {
        // Initialize size mappings (adjust based on your actual size IDs)
        SIZE_MAP.put("1", "XS");
        SIZE_MAP.put("2", "S");
        SIZE_MAP.put("3", "M");
        SIZE_MAP.put("4", "L");
        SIZE_MAP.put("5", "XL");
        SIZE_MAP.put("6", "XXL");
        SIZE_MAP.put("7", "3XL");
        SIZE_MAP.put("8", "4XL");
        SIZE_MAP.put("15", "S");
        SIZE_MAP.put("16", "M");
        SIZE_MAP.put("17", "L");
        SIZE_MAP.put("18", "XL");
        SIZE_MAP.put("52", "L");
        
        // Initialize color mappings (adjust based on your actual color IDs)
        COLOR_MAP.put("1", "Red");
        COLOR_MAP.put("2", "Blue");
        COLOR_MAP.put("3", "Green");
        COLOR_MAP.put("4", "Black");
        COLOR_MAP.put("5", "White");
        COLOR_MAP.put("6", "Yellow");
        COLOR_MAP.put("7", "Pink");
        COLOR_MAP.put("8", "Purple");
        COLOR_MAP.put("9", "Orange");
        COLOR_MAP.put("10", "Brown");
        COLOR_MAP.put("11", "Gray");
        COLOR_MAP.put("12", "Navy");
        COLOR_MAP.put("13", "Maroon");
        COLOR_MAP.put("14", "Beige");
        COLOR_MAP.put("15", "Cream");
        COLOR_MAP.put("16", "Yellow");
        COLOR_MAP.put("17", "Orange");
        COLOR_MAP.put("18", "Pink");
        COLOR_MAP.put("19", "Purple");
        COLOR_MAP.put("20", "Blue");
        COLOR_MAP.put("21", "Green");
        COLOR_MAP.put("22", "Navy");
        COLOR_MAP.put("23", "Sky Blue");
        COLOR_MAP.put("30", "Yellow");
        COLOR_MAP.put("32", "Cyan");
        COLOR_MAP.put("38", "Cream");
        COLOR_MAP.put("60", "White");
    }

    public String getSizeName(String sizeIdOrName) {
        if (sizeIdOrName == null || sizeIdOrName.trim().isEmpty()) {
            return sizeIdOrName;
        }
        
        // If it's already a name (contains letters), return as-is
        if (sizeIdOrName.matches(".*[a-zA-Z].*")) {
            return sizeIdOrName;
        }
        
        // Try to map ID to name
        return SIZE_MAP.getOrDefault(sizeIdOrName, sizeIdOrName);
    }

    public String getColorName(String colorIdOrName) {
        if (colorIdOrName == null || colorIdOrName.trim().isEmpty()) {
            return colorIdOrName;
        }
        
        // If it's already a name (contains letters), return as-is
        if (colorIdOrName.matches(".*[a-zA-Z].*")) {
            return colorIdOrName;
        }
        
        // Try to map ID to name
        return COLOR_MAP.getOrDefault(colorIdOrName, colorIdOrName);
    }

    // Add methods to update mappings dynamically if needed
    public void addSizeMapping(String id, String name) {
        SIZE_MAP.put(id, name);
    }

    public void addColorMapping(String id, String name) {
        COLOR_MAP.put(id, name);
    }
}
