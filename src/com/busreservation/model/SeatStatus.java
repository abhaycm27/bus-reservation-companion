package com.busreservation.model;

/**
 * Lifecycle states of a Seat in the reservation cycle.
 */
public enum SeatStatus {
    AVAILABLE,
    LOCKED, // Temporarily reserved during checkout flow
    BOOKED // Finalized booking status
}
