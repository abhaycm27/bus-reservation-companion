package com.busreservation.exception;

/**
 * Thrown when comfort score metrics lie outside valid boundaries.
 */
public class InvalidRatingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidRatingException(String message) {
        super(message);
    }
}
