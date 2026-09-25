package com.busreservation.service;

import com.busreservation.model.*;
import com.busreservation.repository.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Service managing comfort ratings submitted by verified passengers.
 */
public class ComfortIndexService {
    private final Repository<Passenger, String> passengerRepository;
    private final Repository<Schedule, String> scheduleRepository;
    private final Repository<Booking, String> bookingRepository;

    public ComfortIndexService(Repository<Passenger, String> passengerRepository,
            Repository<Schedule, String> scheduleRepository,
            Repository<Booking, String> bookingRepository) {
        this.passengerRepository = passengerRepository;
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Submits a rating for a trip's comfort index. Verifies the passenger booked
     * the trip.
     */
    public ComfortRating submitComfortRating(String passengerId, String scheduleId,
            double acRating, double seatSpacingRating, double cleanlinessRating) {

        // 1. Verify entities exist
        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found: " + passengerId));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        // 2. Verify passenger actually booked this trip (Verification check)
        List<Booking> bookings = bookingRepository.findAll();
        boolean verified = bookings.stream().anyMatch(b -> b.getPassengerId().equals(passengerId) &&
                b.getScheduleId().equals(scheduleId) &&
                (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.RESCUED));

        if (!verified) {
            throw new SecurityException("Passenger " + passengerId + " is not verified for Schedule " + scheduleId
                    + " (No active booking found).");
        }

        // 3. Create rating
        String ratingId = "RAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ComfortRating rating = new ComfortRating(ratingId, passengerId, scheduleId, acRating, seatSpacingRating,
                cleanlinessRating);

        // 4. Attach rating to the bus of the schedule
        Bus bus = schedule.getBus();
        bus.addComfortRating(rating);

        return rating;
    }

    /**
     * Gets the current overall comfort index score for a specific bus.
     */
    public double getBusComfortScore(Bus bus) {
        if (bus == null)
            throw new IllegalArgumentException("Bus cannot be null");
        return bus.getComfortScore();
    }
}
