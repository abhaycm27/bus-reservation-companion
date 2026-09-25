package com.busreservation.model;

import java.util.*;

/**
 * RestStop model representing a rest and food stop along a route with safety
 * ratings and options.
 */
public class RestStop {
    private final String restStopId;
    private final String name;
    private double hygieneRating;
    private final Set<DietaryOption> dietaryOptions;
    private final List<String> passengerReviews;

    public RestStop(String restStopId, String name, double hygieneRating) {
        if (restStopId == null || restStopId.isBlank())
            throw new IllegalArgumentException("Rest stop ID cannot be empty");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Rest stop name cannot be empty");
        if (hygieneRating < 0.0 || hygieneRating > 5.0)
            throw new IllegalArgumentException("Hygiene rating must be between 0.0 and 5.0");

        this.restStopId = restStopId;
        this.name = name;
        this.hygieneRating = hygieneRating;
        this.dietaryOptions = new HashSet<>();
        this.passengerReviews = new ArrayList<>();
    }

    public String getRestStopId() {
        return restStopId;
    }

    public String getName() {
        return name;
    }

    public double getHygieneRating() {
        return hygieneRating;
    }

    public void setHygieneRating(double hygieneRating) {
        if (hygieneRating < 0.0 || hygieneRating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }
        this.hygieneRating = hygieneRating;
    }

    public Set<DietaryOption> getDietaryOptions() {
        return new HashSet<>(dietaryOptions);
    }

    public void addDietaryOption(DietaryOption option) {
        if (option != null) {
            this.dietaryOptions.add(option);
        }
    }

    public List<String> getPassengerReviews() {
        return new ArrayList<>(passengerReviews);
    }

    public void addPassengerReview(String review) {
        if (review != null && !review.isBlank()) {
            this.passengerReviews.add(review);
        }
    }
}
