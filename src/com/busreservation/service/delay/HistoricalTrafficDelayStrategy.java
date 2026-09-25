package com.busreservation.service.delay;

import com.busreservation.model.Route;

/**
 * Predicts delays based on typical historical patterns, distance, and live
 * traffic congestion factor.
 */
public class HistoricalTrafficDelayStrategy implements DelayPredictionStrategy {
    @Override
    public int predictDelay(Route route, double trafficFactor, String weatherCondition) {
        // Traffic factor scales the delay: 1.0 (empty), 2.0 (moderate), 3.0+ (gridlock)
        double distanceKm = route.getDistanceKm();
        // Assume 5 minutes of base delay for every 50 KM of travel under empty
        // conditions
        double baseDelay = (distanceKm / 50.0) * 5.0;
        return (int) Math.round(baseDelay * trafficFactor);
    }
}
