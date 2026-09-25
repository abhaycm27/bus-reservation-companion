package com.busreservation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Bus entity representing a vehicle in the fleet with seat layouts, comfort
 * ratings, and drivers.
 */
public class Bus {
    private final String busId;
    private final String registrationNumber;
    private final String operatorName;
    private final Set<BusFeature> features;
    private final List<String> photos; // Photo URLs
    private final SeatMap seatMap;
    private Driver assignedDriver;
    private double comfortScore; // Aggregated score out of 5.0
    private final List<ComfortRating> comfortRatings; // List of ratings given to this bus

    public Bus(String busId, String registrationNumber, String operatorName, SeatMap seatMap) {
        if (busId == null || busId.isBlank())
            throw new IllegalArgumentException("Bus ID cannot be empty");
        if (registrationNumber == null || registrationNumber.isBlank())
            throw new IllegalArgumentException("Registration Number cannot be empty");
        if (operatorName == null || operatorName.isBlank())
            throw new IllegalArgumentException("Operator name cannot be empty");
        if (seatMap == null)
            throw new IllegalArgumentException("SeatMap cannot be null");

        this.busId = busId;
        this.registrationNumber = registrationNumber;
        this.operatorName = operatorName;
        this.seatMap = seatMap;
        this.features = new HashSet<>();
        this.photos = new ArrayList<>();
        this.assignedDriver = null;
        this.comfortScore = 0.0;
        this.comfortRatings = new ArrayList<>();
    }

    public String getBusId() {
        return busId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public SeatMap getSeatMap() {
        return seatMap;
    }

    public synchronized Driver getAssignedDriver() {
        return assignedDriver;
    }

    public synchronized void setAssignedDriver(Driver assignedDriver) {
        this.assignedDriver = assignedDriver;
    }

    public synchronized Set<BusFeature> getFeatures() {
        return new HashSet<>(features);
    }

    public synchronized void addFeature(BusFeature feature) {
        if (feature != null) {
            this.features.add(feature);
        }
    }

    public synchronized List<String> getPhotos() {
        return new ArrayList<>(photos);
    }

    public synchronized void addPhotoUrl(String photoUrl) {
        if (photoUrl != null && !photoUrl.isBlank()) {
            this.photos.add(photoUrl);
        }
    }

    public synchronized double getComfortScore() {
        return comfortScore;
    }

    public synchronized List<ComfortRating> getComfortRatings() {
        return new ArrayList<>(comfortRatings);
    }

    /**
     * Thread-safely records a passenger comfort rating and recomputes comfortScore.
     */
    public synchronized void addComfortRating(ComfortRating rating) {
        if (rating == null)
            throw new IllegalArgumentException("Rating cannot be null");
        this.comfortRatings.add(rating);
        recalculateComfortScore();
    }

    private void recalculateComfortScore() {
        if (comfortRatings.isEmpty()) {
            this.comfortScore = 0.0;
            return;
        }
        double sum = 0.0;
        for (ComfortRating rating : comfortRatings) {
            sum += rating.getComfortScore();
        }
        this.comfortScore = sum / comfortRatings.size();
    }
}
