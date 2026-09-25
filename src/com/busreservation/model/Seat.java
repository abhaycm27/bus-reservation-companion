package com.busreservation.model;

import java.time.Instant;

/**
 * Thread-safe representation of a Bus Seat displaying encapsulate state,
 * pricing strategies, and lock expirations.
 */
public class Seat {
    private final String seatNumber;
    private final SeatType seatType;
    private SeatStatus status;
    private final double priceModifier;
    private String lockedByPassengerId;
    private Instant lockExpiration;
    private final int row;       // Row number in bus layout
    private final int column;    // Column position (0=left, 1=left-aisle, 2=right-aisle, 3=right)
    private final String position; // Position label ("window-left", "aisle-left", "aisle-right", "window-right")

    public Seat(String seatNumber, SeatType seatType) {
        this(seatNumber, seatType, seatType.getPriceModifier(), 0, 0, "window-left");
    }

    public Seat(String seatNumber, SeatType seatType, double priceModifier) {
        this(seatNumber, seatType, priceModifier, 0, 0, "window-left");
    }

    public Seat(String seatNumber, SeatType seatType, int row, int column, String position) {
        this(seatNumber, seatType, seatType.getPriceModifier(), row, column, position);
    }

    public Seat(String seatNumber, SeatType seatType, double priceModifier, int row, int column, String position) {
        if (seatNumber == null || seatNumber.isBlank()) {
            throw new IllegalArgumentException("Seat number cannot be empty or null");
        }
        if (seatType == null) {
            throw new IllegalArgumentException("Seat type cannot be null");
        }
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.status = SeatStatus.AVAILABLE;
        this.priceModifier = priceModifier;
        this.lockedByPassengerId = null;
        this.lockExpiration = null;
        this.row = row;
        this.column = column;
        this.position = position;
    }

    /**
     * Attempts to thread-safely lock this seat for a specific passenger.
     */
    public synchronized boolean lockSeat(String passengerId, Instant expirationTime) {
        Instant now = Instant.now();
        if (isAvailable(now)) {
            this.status = SeatStatus.LOCKED;
            this.lockedByPassengerId = passengerId;
            this.lockExpiration = expirationTime;
            return true;
        }
        return false;
    }

    /**
     * Releases any current temporary lock on the seat.
     */
    public synchronized boolean unlockSeat() {
        if (this.status == SeatStatus.LOCKED || this.status == SeatStatus.BOOKED) {
            this.status = SeatStatus.AVAILABLE;
            this.lockedByPassengerId = null;
            this.lockExpiration = null;
            return true;
        }
        return false;
    }

    /**
     * Confirms the booking status of the seat for the lock owner.
     */
    public synchronized boolean bookSeat(String passengerId) {
        Instant now = Instant.now();
        if (this.status == SeatStatus.BOOKED) {
            return false;
        }
        // Can book if locked by this passenger and lock hasn't expired
        if (this.status == SeatStatus.LOCKED && passengerId.equals(this.lockedByPassengerId)) {
            if (lockExpiration != null && now.isAfter(lockExpiration)) {
                // Lock expired
                this.status = SeatStatus.AVAILABLE;
                this.lockedByPassengerId = null;
                this.lockExpiration = null;
                return false;
            }
            this.status = SeatStatus.BOOKED;
            this.lockExpiration = null;
            return true;
        }
        // Direct booking if available
        if (isAvailable(now)) {
            this.status = SeatStatus.BOOKED;
            this.lockedByPassengerId = passengerId;
            this.lockExpiration = null;
            return true;
        }
        return false;
    }

    /**
     * Check availability taking lock expiration into account.
     */
    public synchronized boolean isAvailable(Instant now) {
        if (this.status == SeatStatus.AVAILABLE) {
            return true;
        }
        if (this.status == SeatStatus.LOCKED) {
            if (lockExpiration != null && now.isAfter(lockExpiration)) {
                // Auto-expire lock and make available
                this.status = SeatStatus.AVAILABLE;
                this.lockedByPassengerId = null;
                this.lockExpiration = null;
                return true;
            }
        }
        return false;
    }

    // Getters
    public String getSeatNumber() {
        return seatNumber;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public synchronized SeatStatus getStatus() {
        // Auto-update internal status if lock is expired
        isAvailable(Instant.now());
        return status;
    }

    public double getPriceModifier() {
        return priceModifier;
    }

    public synchronized String getLockedByPassengerId() {
        return lockedByPassengerId;
    }

    public synchronized Instant getLockExpiration() {
        return lockExpiration;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String getPosition() {
        return position;
    }
}
