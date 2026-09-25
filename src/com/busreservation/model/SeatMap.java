package com.busreservation.model;

import java.time.Instant;
import java.util.*;

/**
 * SeatMap manages the collection of Seat entities on a bus, showing layout
 * previews.
 */
public class SeatMap {
    private final Map<String, Seat> seats; // Layout mapped by Seat Number (e.g. "1A", "1B")

    public SeatMap(List<Seat> seatList) {
        if (seatList == null || seatList.isEmpty()) {
            throw new IllegalArgumentException("Seat list cannot be null or empty");
        }
        this.seats = new LinkedHashMap<>();
        for (Seat seat : seatList) {
            this.seats.put(seat.getSeatNumber(), seat);
        }
    }

    public synchronized Optional<Seat> getSeat(String seatNumber) {
        return Optional.ofNullable(seats.get(seatNumber));
    }

    public synchronized Collection<Seat> getAllSeats() {
        return new ArrayList<>(seats.values());
    }

    public synchronized List<Seat> getAvailableSeats() {
        Instant now = Instant.now();
        List<Seat> available = new ArrayList<>();
        for (Seat seat : seats.values()) {
            if (seat.isAvailable(now)) {
                available.add(seat);
            }
        }
        return available;
    }

    public synchronized int getAvailableCount() {
        Instant now = Instant.now();
        int count = 0;
        for (Seat seat : seats.values()) {
            if (seat.isAvailable(now)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Generates a textual interactive representation of the Seat Map showing seat
     * coordinates.
     */
    public synchronized String displayLayout() {
        StringBuilder builder = new StringBuilder();
        builder.append("=== SEAT MAP LAYOUT ===\n");
        int count = 0;
        Instant now = Instant.now();
        for (Map.Entry<String, Seat> entry : seats.entrySet()) {
            Seat seat = entry.getValue();
            String statusChar = "A"; // Available
            if (seat.getStatus() == SeatStatus.BOOKED) {
                statusChar = "B"; // Booked
            } else if (seat.getStatus() == SeatStatus.LOCKED && !seat.isAvailable(now)) {
                statusChar = "L"; // Locked
            }

            builder.append(String.format("[%s:%s(%s)]  ", seat.getSeatNumber(), seat.getSeatType().name().charAt(0),
                    statusChar));
            count++;
            if (count % 4 == 0) {
                builder.append("\n"); // Format layout grid: 4 seats per row
            }
        }
        if (count % 4 != 0) {
            builder.append("\n");
        }
        builder.append(
                "Legend: [Seat:Type(Status)] | Types: W=Window, A=Aisle, S=Sleeper | Status: A=Available, L=Locked, B=Booked\n");
        return builder.toString();
    }
}
