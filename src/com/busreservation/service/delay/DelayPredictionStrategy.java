package com.busreservation.service.delay;

import com.busreservation.model.Route;

/**
 * Strategy interface for predicting route delays.
 */
public interface DelayPredictionStrategy {
    /**
     * Calculates the estimated delay in minutes based on traffic and weather
     * parameters.
     */
    int predictDelay(Route route, double trafficFactor, String weatherCondition);
}
