package com.busreservation.model;

import com.busreservation.exception.InvalidRatingException;

/**
 * ComfortRating holds multi-dimensional assessment metrics from verified users.
 */
public class ComfortRating {
    private final String ratingId;
    private final String passengerId;
    private final String scheduleId;
    private final double acRating;
    private final double seatSpacingRating;
    private final double cleanlinessRating;
    private final double comfortScore; // Aggregated score (average)

    public ComfortRating(String ratingId, String passengerId, String scheduleId,
            double acRating, double seatSpacingRating, double cleanlinessRating) {
        if (ratingId == null || ratingId.isBlank())
            throw new IllegalArgumentException("Rating ID cannot be empty");
        if (passengerId == null || passengerId.isBlank())
            throw new IllegalArgumentException("Passenger ID cannot be empty");
        if (scheduleId == null || scheduleId.isBlank())
            throw new IllegalArgumentException("Schedule ID cannot be empty");

        validateRatingValue("acRating", acRating);
        validateRatingValue("seatSpacingRating", seatSpacingRating);
        validateRatingValue("cleanlinessRating", cleanlinessRating);

        this.ratingId = ratingId;
        this.passengerId = passengerId;
        this.scheduleId = scheduleId;
        this.acRating = acRating;
        this.seatSpacingRating = seatSpacingRating;
        this.cleanlinessRating = cleanlinessRating;
        this.comfortScore = (acRating + seatSpacingRating + cleanlinessRating) / 3.0;
    }

    private void validateRatingValue(String fieldName, double val) {
        if (val < 0.0 || val > 5.0) {
            throw new InvalidRatingException(fieldName + " value " + val + " must be between 0.0 and 5.0 inclusive");
        }
    }

    public String getRatingId() {
        return ratingId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public double getAcRating() {
        return acRating;
    }

    public double getSeatSpacingRating() {
        return seatSpacingRating;
    }

    public double getCleanlinessRating() {
        return cleanlinessRating;
    }

    public double getComfortScore() {
        return comfortScore;
    }
}
