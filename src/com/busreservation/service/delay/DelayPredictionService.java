package com.busreservation.service.delay;

import com.busreservation.model.Route;

/**
 * Service orchestrating delay predictions using different Strategy
 * configurations.
 */
public class DelayPredictionService {
    private DelayPredictionStrategy strategy;

    public DelayPredictionService(DelayPredictionStrategy strategy) {
        if (strategy == null)
            throw new IllegalArgumentException("Strategy cannot be null");
        this.strategy = strategy;
    }

    public synchronized void setStrategy(DelayPredictionStrategy strategy) {
        if (strategy == null)
            throw new IllegalArgumentException("Strategy cannot be null");
        this.strategy = strategy;
    }

    public synchronized int predictDelayMinutes(Route route, double trafficFactor, String weatherCondition) {
        if (route == null)
            throw new IllegalArgumentException("Route cannot be null");
        return this.strategy.predictDelay(route, trafficFactor, weatherCondition);
    }
}
