package com.busreservation.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Route entity detailing source, destination, distance, and planned rest stops.
 */
public class Route {
    private final String routeId;
    private final String source;
    private final String destination;
    private final double distanceKm;
    private final List<RestStop> restStops;

    public Route(String routeId, String source, String destination, double distanceKm) {
        if (routeId == null || routeId.isBlank())
            throw new IllegalArgumentException("Route ID cannot be empty");
        if (source == null || source.isBlank())
            throw new IllegalArgumentException("Source cannot be empty");
        if (destination == null || destination.isBlank())
            throw new IllegalArgumentException("Destination cannot be empty");
        if (distanceKm <= 0)
            throw new IllegalArgumentException("Distance must be greater than zero");

        this.routeId = routeId;
        this.source = source;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.restStops = new ArrayList<>();
    }

    public String getRouteId() {
        return routeId;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public List<RestStop> getRestStops() {
        return new ArrayList<>(restStops);
    }

    public void addRestStop(RestStop stop) {
        if (stop != null) {
            this.restStops.add(stop);
        }
    }
}
