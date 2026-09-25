package com.busreservation.service;

import com.busreservation.model.*;
import com.busreservation.repository.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service managing user-uploaded travel photos and media galleries.
 */
public class TripGalleryService {
    private final Repository<Passenger, String> passengerRepository;
    private final Repository<Schedule, String> scheduleRepository;
    private final Repository<Booking, String> bookingRepository;
    private final Repository<TripPhoto, String> photoRepository;

    public TripGalleryService(Repository<Passenger, String> passengerRepository,
            Repository<Schedule, String> scheduleRepository,
            Repository<Booking, String> bookingRepository,
            Repository<TripPhoto, String> photoRepository) {
        this.passengerRepository = passengerRepository;
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
        this.photoRepository = photoRepository;
    }

    /**
     * Uploads passenger photos from finished or active schedules.
     */
    public TripPhoto uploadTripPhoto(String passengerId, String scheduleId, String caption, String photoUrl) {
        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found: " + passengerId));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        // 1. Verify traveler booking
        boolean verified = bookingRepository.findAll().stream().anyMatch(b -> b.getPassengerId().equals(passengerId) &&
                b.getScheduleId().equals(scheduleId) &&
                (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.RESCUED));

        if (!verified) {
            throw new SecurityException("Passenger " + passengerId + " is not authorized to upload photos for trip "
                    + scheduleId + " (No active booking).");
        }

        // 2. Verify trip has commenced / completed (departure is in the past)
        if (schedule.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "You can only upload photos for active or completed trips. Departure scheduled at: "
                            + schedule.getDepartureTime());
        }

        // 3. Create photo record
        String photoId = "PHT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        TripPhoto photo = new TripPhoto(photoId, passengerId, scheduleId, caption, photoUrl);

        // 4. Save photo
        photoRepository.save(photo);
        passenger.addTripPhoto(photo);
        passengerRepository.save(passenger);

        return photo;
    }

    /**
     * Retrieves all photos uploaded for a specific schedule trip.
     */
    public List<TripPhoto> getTripGallery(String scheduleId) {
        if (scheduleId == null)
            throw new IllegalArgumentException("Schedule ID cannot be null");
        return photoRepository.findAll().stream()
                .filter(photo -> photo.getTripId().equals(scheduleId))
                .toList();
    }
}
