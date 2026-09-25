package com.busreservation.model;

import java.time.LocalDateTime;

/**
 * Schedule represents a specific occurrence of a bus running along a route.
 */
public class Schedule {
    private final String scheduleId;
    private final Bus bus;
    private final Route route;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private final double baseFare;

    public Schedule(String scheduleId, Bus bus, Route route, LocalDateTime departureTime, LocalDateTime arrivalTime,
            double baseFare) {
        if (scheduleId == null || scheduleId.isBlank())
            throw new IllegalArgumentException("Schedule ID cannot be empty");
        if (bus == null)
            throw new IllegalArgumentException("Bus cannot be null");
        if (route == null)
            throw new IllegalArgumentException("Route cannot be null");
        if (departureTime == null || arrivalTime == null)
            throw new IllegalArgumentException("Times cannot be null");
        if (arrivalTime.isBefore(departureTime))
            throw new IllegalArgumentException("Arrival time cannot be before departure time");
        if (baseFare <= 0)
            throw new IllegalArgumentException("Base fare must be positive");

        this.scheduleId = scheduleId;
        this.bus = bus;
        this.route = route;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.baseFare = baseFare;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public Bus getBus() {
        return bus;
    }

    public Route getRoute() {
        return route;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getBaseFare() {
        return baseFare;
    }
}
