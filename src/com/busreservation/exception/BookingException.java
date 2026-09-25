package com.busreservation.exception;

/**
 * General exception representing billing or processing failures in seats or
 * bookings.
 */
public class BookingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BookingException(String message) {
        super(message);
    }
}
