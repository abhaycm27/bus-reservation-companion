package com.busreservation.model;

import java.time.Instant;

/**
 * TripPhoto represents trip memories uploaded to the passenger photo stream.
 */
public class TripPhoto {
    private final String photoId;
    private final String passengerId;
    private final String tripId; // Maps to schedule ID
    private final String caption;
    private final String photoUrl;
    private final Instant timestamp;
    private int likeCount;

    public TripPhoto(String photoId, String passengerId, String tripId, String caption, String photoUrl) {
        if (photoId == null || photoId.isBlank())
            throw new IllegalArgumentException("Photo ID cannot be empty");
        if (passengerId == null || passengerId.isBlank())
            throw new IllegalArgumentException("Passenger ID cannot be empty");
        if (tripId == null || tripId.isBlank())
            throw new IllegalArgumentException("Trip ID cannot be empty");
        if (photoUrl == null || photoUrl.isBlank())
            throw new IllegalArgumentException("Photo URL cannot be empty");

        this.photoId = photoId;
        this.passengerId = passengerId;
        this.tripId = tripId;
        this.caption = caption != null ? caption : "";
        this.photoUrl = photoUrl;
        this.timestamp = Instant.now();
        this.likeCount = 0;
    }

    public String getPhotoId() {
        return photoId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getCaption() {
        return caption;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public synchronized void like() {
        this.likeCount++;
    }

    public synchronized void unlike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
