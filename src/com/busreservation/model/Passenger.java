package com.busreservation.model;

import java.util.ArrayList;
import java.util.List;
import com.busreservation.service.rescue.Observer;

/**
 * Passenger model inheriting from User. Holds trip history, reward points,
 * uploaded media, and receives alerts.
 */
public class Passenger extends User implements Observer {
    private int rewardPoints;
    private final List<String> bookingHistoryIds; // References to Booking IDs
    private final List<TripPhoto> tripMedia; // Uploaded photos
    private final List<String> notifications; // List of notification messages received

    public Passenger(String userId, String name, String email, String phone, String authCredentials) {
        super(userId, name, email, phone, authCredentials);
        this.rewardPoints = 0;
        this.bookingHistoryIds = new ArrayList<>();
        this.tripMedia = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public synchronized void addRewardPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points to add cannot be negative");
        }
        this.rewardPoints += points;
    }

    public synchronized void deductRewardPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points to deduct cannot be negative");
        }
        if (this.rewardPoints < points) {
            throw new IllegalArgumentException("Insufficient reward points to deduct");
        }
        this.rewardPoints -= points;
    }

    public synchronized List<String> getBookingHistoryIds() {
        return new ArrayList<>(bookingHistoryIds);
    }

    public synchronized void addBookingToHistory(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID cannot be empty");
        }
        this.bookingHistoryIds.add(bookingId);
    }

    public synchronized List<TripPhoto> getTripMedia() {
        return new ArrayList<>(tripMedia);
    }

    public synchronized void addTripPhoto(TripPhoto photo) {
        if (photo == null) {
            throw new IllegalArgumentException("Trip photo cannot be null");
        }
        this.tripMedia.add(photo);
    }

    // Observer Pattern implementation
    @Override
    public synchronized void onNotificationReceived(String category, String message) {
        String formattedMsg = String.format("[%s Alert] %s", category, message);
        this.notifications.add(formattedMsg);
        System.out.println("Notification sent to " + getName() + " (" + getEmail() + "): " + formattedMsg);
    }

    public synchronized List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }
}
