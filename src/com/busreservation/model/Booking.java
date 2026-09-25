package com.busreservation.model;

import java.time.Instant;

/**
 * Booking represents a finalized reservation for a specific Seat and Passenger.
 */
public class Booking {
    private final String bookingId;
    private final String passengerId;
    private final String scheduleId;
    private final String seatNumber;
    private double finalPrice;
    private BookingStatus status;
    private final Instant bookingTime;
    private String paymentTransactionId;
    private String rescuedToBookingId; // References replacement bookingId if status is RESCUED

    public Booking(String bookingId, String passengerId, String scheduleId, String seatNumber, double finalPrice) {
        if (bookingId == null || bookingId.isBlank())
            throw new IllegalArgumentException("Booking ID cannot be empty");
        if (passengerId == null || passengerId.isBlank())
            throw new IllegalArgumentException("Passenger ID cannot be empty");
        if (scheduleId == null || scheduleId.isBlank())
            throw new IllegalArgumentException("Schedule ID cannot be empty");
        if (seatNumber == null || seatNumber.isBlank())
            throw new IllegalArgumentException("Seat number cannot be empty");
        if (finalPrice < 0.0)
            throw new IllegalArgumentException("Final price cannot be negative");

        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.scheduleId = scheduleId;
        this.seatNumber = seatNumber;
        this.finalPrice = finalPrice;
        this.status = BookingStatus.PENDING;
        this.bookingTime = Instant.now();
        this.paymentTransactionId = null;
        this.rescuedToBookingId = null;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        if (finalPrice < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.finalPrice = finalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Instant getBookingTime() {
        return bookingTime;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public String getRescuedToBookingId() {
        return rescuedToBookingId;
    }

    public void setRescuedToBookingId(String rescuedToBookingId) {
        this.rescuedToBookingId = rescuedToBookingId;
    }
}
