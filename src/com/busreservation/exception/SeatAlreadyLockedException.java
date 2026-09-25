package com.busreservation.exception;

/**
 * Thrown when trying to lock or book a seat that is currently locked by another
 * passenger or booked.
 */
public class SeatAlreadyLockedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SeatAlreadyLockedException(String message) {
        super(message);
    }
}
