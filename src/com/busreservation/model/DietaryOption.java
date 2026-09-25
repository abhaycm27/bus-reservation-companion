package com.busreservation.model;

/**
 * Dietary options supported at food/rest stops.
 */
public enum DietaryOption {
    VEG("Vegetarian"),
    JAIN("Jain Food"),
    NON_VEG("Non-Vegetarian");

    private final String displayName;

    DietaryOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
