package com.busreservation.service;

import com.busreservation.exception.BookingException;
import com.busreservation.exception.SeatAlreadyLockedException;
import com.busreservation.model.*;
import com.busreservation.repository.Repository;
import com.busreservation.service.payment.PaymentMethod;
import com.busreservation.service.payment.PaymentProcessor;
import com.busreservation.service.payment.PlatformPaymentFactory;
import com.busreservation.service.payment.PaymentReceipt;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Service managing seat locks, ticket bookings, payment processing, and
 * cancellations.
 */
public class ReservationService {
    private final Repository<Booking, String> bookingRepository;
    private final Repository<Schedule, String> scheduleRepository;
    private final Repository<Passenger, String> passengerRepository;

    public ReservationService(Repository<Booking, String> bookingRepository,
            Repository<Schedule, String> scheduleRepository,
            Repository<Passenger, String> passengerRepository) {
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
        this.passengerRepository = passengerRepository;
    }

    /**
     * Threads-safely locks a seat for a passenger for 10 minutes.
     */
    public boolean lockSeat(String scheduleId, String seatNumber, String passengerId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        Seat seat = schedule.getBus().getSeatMap().getSeat(seatNumber)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatNumber));

        Instant expiration = Instant.now().plus(Duration.ofMinutes(10));
        boolean locked = seat.lockSeat(passengerId, expiration);
        if (!locked) {
            String lockedBy = seat.getLockedByPassengerId();
            throw new SeatAlreadyLockedException(
                    String.format("Seat %s is currently locked by passenger ID %s until %s",
                            seatNumber, lockedBy, seat.getLockExpiration()));
        }
        return true;
    }

    /**
     * Confirms booking and processes payment.
     */
    public Booking confirmBooking(String passengerId, String scheduleId, String seatNumber,
            PaymentMethod paymentMethod, String paymentDetails) {
        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found: " + passengerId));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        Seat seat = schedule.getBus().getSeatMap().getSeat(seatNumber)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatNumber));

        // Calculate dynamic final fare based on base fare and seat price modifier
        double finalPrice = schedule.getBaseFare() * seat.getPriceModifier();

        // Process payment using Factory and Strategy patterns
        PaymentProcessor paymentProcessor = PlatformPaymentFactory.getPaymentProcessor(paymentMethod);
        PaymentReceipt receipt = paymentProcessor.pay(finalPrice, paymentDetails);

        if (!receipt.isSuccess()) {
            // Unlock seat if it was locked by this user
            if (passengerId.equals(seat.getLockedByPassengerId())) {
                seat.unlockSeat();
            }
            throw new BookingException("Payment processing failed. Transaction declined.");
        }

        // Attempt to book the seat (locks details into Booked status)
        boolean booked = seat.bookSeat(passengerId);
        if (!booked) {
            throw new BookingException("Unable to book seat. Seat may have expired or been locked by another user.");
        }

        // Generate Booking
        String bookingId = "BKG-" + System.currentTimeMillis() + "-" + seatNumber;
        Booking booking = new Booking(bookingId, passengerId, scheduleId, seatNumber, finalPrice);
        booking.setPaymentTransactionId(receipt.getTransactionId());
        booking.setStatus(BookingStatus.CONFIRMED);

        // Update database
        bookingRepository.save(booking);

        // Update Passenger (reward points: 10% of finalPrice)
        passenger.addBookingToHistory(bookingId);
        int pointsEarned = (int) (finalPrice * 0.1);
        passenger.addRewardPoints(pointsEarned);
        passengerRepository.save(passenger);

        // Also add Schedule back to Passenger's completed trip list if needed
        // (simulated)

        return booking;
    }

    /**
     * Cancels a booking, refunds/releases seat layout.
     */
    public void cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return; // Already cancelled
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Fetch seat components and release
        Schedule schedule = scheduleRepository.findById(booking.getScheduleId()).orElse(null);
        if (schedule != null) {
            Optional<Seat> seatOpt = schedule.getBus().getSeatMap().getSeat(booking.getSeatNumber());
            seatOpt.ifPresent(Seat::unlockSeat);
        }
    }
}
