package com.busreservation.service.rescue;

import com.busreservation.exception.AutoRescueFailureException;
import com.busreservation.model.*;
import com.busreservation.repository.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service managing automated breakdown rescue flows, alternate fleet checkups,
 * and passenger notifications.
 */
public class AutoRescueService {
    private final Repository<Booking, String> bookingRepository;
    private final Repository<Schedule, String> scheduleRepository;
    private final Repository<Passenger, String> passengerRepository;

    public AutoRescueService(Repository<Booking, String> bookingRepository,
            Repository<Schedule, String> scheduleRepository,
            Repository<Passenger, String> passengerRepository) {
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
        this.passengerRepository = passengerRepository;
    }

    /**
     * Scans for broken routes, triggers re-booking workflows, and alerts affected
     * users.
     */
    public synchronized List<Booking> triggerRescue(String brokenScheduleId) {
        Schedule brokenSchedule = scheduleRepository.findById(brokenScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Broken schedule not found: " + brokenScheduleId));

        Route route = brokenSchedule.getRoute();

        // Find all confirmed bookings on the broken schedule
        List<Booking> affectedBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getScheduleId().equals(brokenScheduleId) && b.getStatus() == BookingStatus.CONFIRMED)
                .toList();

        if (affectedBookings.isEmpty()) {
            System.out.println("[Auto-Rescue] No bookings affected by breakdown of schedule " + brokenScheduleId);
            return new ArrayList<>();
        }

        List<Booking> rescuedBookings = new ArrayList<>();
        List<String> failedRescuePassengerNames = new ArrayList<>();

        for (Booking booking : affectedBookings) {
            Passenger passenger = passengerRepository.findById(booking.getPassengerId()).orElse(null);

            if (passenger != null) {
                // Notify passenger about breakdown (Observer Pattern)
                passenger.onNotificationReceived("BREAKDOWN", "Your bus schedule " + brokenScheduleId +
                        " has encountered a critical breakdown. We are searching for emergency rescue alternatives...");
            }

            // Look up alternative schedules matching route
            Schedule alternateSchedule = findAlternateSchedule(brokenScheduleId, route);
            if (alternateSchedule == null) {
                // Cancel booking, handle refund
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);

                // Release old seat
                Optional<Seat> seatOpt = brokenSchedule.getBus().getSeatMap().getSeat(booking.getSeatNumber());
                seatOpt.ifPresent(Seat::unlockSeat);

                if (passenger != null) {
                    passenger.onNotificationReceived("AUTO-RESCUE-FAIL",
                            "No alternative bus seats are available along this route. Your booking has been refunded.");
                    failedRescuePassengerNames.add(passenger.getName());
                }
                continue;
            }

            // Assign seat in alternative bus
            List<Seat> availableSeats = alternateSchedule.getBus().getSeatMap().getAvailableSeats();
            if (availableSeats.isEmpty()) {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);

                Optional<Seat> seatOpt = brokenSchedule.getBus().getSeatMap().getSeat(booking.getSeatNumber());
                seatOpt.ifPresent(Seat::unlockSeat);

                if (passenger != null) {
                    passenger.onNotificationReceived("AUTO-RESCUE-FAIL",
                            "No seats left on alternative schedule. Booking refunded.");
                    failedRescuePassengerNames.add(passenger.getName());
                }
                continue;
            }

            // Get first available seat in alternate bus
            Seat targetSeat = availableSeats.get(0);
            boolean booked = targetSeat.bookSeat(booking.getPassengerId());
            if (booked) {
                // Save reservation details
                String rescueBookingId = "RSC-" + System.currentTimeMillis() + "-" + targetSeat.getSeatNumber();
                Booking rescueBooking = new Booking(rescueBookingId, booking.getPassengerId(),
                        alternateSchedule.getScheduleId(), targetSeat.getSeatNumber(), booking.getFinalPrice());
                rescueBooking.setStatus(BookingStatus.CONFIRMED);
                rescueBooking.setPaymentTransactionId("RESCUE-" + booking.getPaymentTransactionId());

                booking.setStatus(BookingStatus.RESCUED);
                booking.setRescuedToBookingId(rescueBookingId);

                bookingRepository.save(booking);
                bookingRepository.save(rescueBooking);

                // Release seat on broken bus
                Optional<Seat> seatOpt = brokenSchedule.getBus().getSeatMap().getSeat(booking.getSeatNumber());
                seatOpt.ifPresent(Seat::unlockSeat);

                if (passenger != null) {
                    // Update Passenger trip history
                    passenger.addBookingToHistory(rescueBookingId);
                    passengerRepository.save(passenger);

                    passenger.onNotificationReceived("AUTO-RESCUE-SUCCESS",
                            String.format(
                                    "Emergency alternate route booked successfully! Bus: %s (%s). Seat: %s. Depart time: %s",
                                    alternateSchedule.getBus().getBusId(),
                                    alternateSchedule.getBus().getRegistrationNumber(),
                                    targetSeat.getSeatNumber(),
                                    alternateSchedule.getDepartureTime()));
                }
                rescuedBookings.add(rescueBooking);
            } else {
                if (passenger != null) {
                    failedRescuePassengerNames.add(passenger.getName());
                }
            }
        }

        if (!failedRescuePassengerNames.isEmpty()) {
            throw new AutoRescueFailureException(
                    "Emergency Auto-Rescue failed for passengers: " + failedRescuePassengerNames);
        }

        return rescuedBookings;
    }

    private Schedule findAlternateSchedule(String currentScheduleId, Route route) {
        List<Schedule> allSchedules = scheduleRepository.findAll();
        for (Schedule s : allSchedules) {
            // Must have matching route source and destination, and must have free seats
            if (!s.getScheduleId().equals(currentScheduleId) &&
                    s.getRoute().getSource().equalsIgnoreCase(route.getSource()) &&
                    s.getRoute().getDestination().equalsIgnoreCase(route.getDestination()) &&
                    s.getBus().getSeatMap().getAvailableCount() > 0) {
                return s;
            }
        }
        return null;
    }
}
