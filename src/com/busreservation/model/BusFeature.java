package com.busreservation.model;

/**
 * Enumeration representing comfort and utility amenities available on a bus.
 */
public enum BusFeature {
    AC("Air Conditioning"),
    WIFI("High-Speed Wi-Fi"),
    CHARGING_PORTS("USB & Plug Charging Ports"),
    RECLINING_SEATS("Reclining Ergonomic Seats"),
    ONBOARD_WASHROOM("On-board Clean Washroom");

    private final String description;

    BusFeature(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
