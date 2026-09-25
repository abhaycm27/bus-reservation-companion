package com.busreservation.model;

/**
 * State of a processed Reservation Booking.
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    RESCUED // Booking has been moved to an alternative schedule due to breakdown
}
