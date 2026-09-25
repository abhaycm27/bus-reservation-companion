package com.busreservation.service.delay;

import com.busreservation.model.Route;

/**
 * Predicts delays based on live weather reports and localized checkpoints
 * alerts.
 */
public class LiveWeatherCheckpointDelayStrategy implements DelayPredictionStrategy {
    @Override
    public int predictDelay(Route route, double trafficFactor, String weatherCondition) {
        String cond = weatherCondition != null ? weatherCondition.toUpperCase() : "SUNNY";
        int weatherImpactMinutes = switch (cond) {
            case "RAINY" -> 12;
            case "FOGGY" -> 20;
            case "STORMY" -> 40;
            case "MONSOON" -> 50;
            default -> 0;
        };
        // Add additional checkpoint checking delay proportional to complexity
        double baseRouteComplexity = route.getDistanceKm() / 100.0;
        int checkpointDelay = (int) (baseRouteComplexity * 5 * trafficFactor);

        return weatherImpactMinutes + checkpointDelay;
    }
}
