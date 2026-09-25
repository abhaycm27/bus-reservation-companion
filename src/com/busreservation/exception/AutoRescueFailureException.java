package com.busreservation.exception;

/**
 * Thrown when auto-rescue fails because no alternative seats match the
 * passenger's route options.
 */
public class AutoRescueFailureException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AutoRescueFailureException(String message) {
        super(message);
    }
}
