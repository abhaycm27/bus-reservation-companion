package com.busreservation.service.rescue;

/**
 * Observer interface in the Observer pattern to receive notifications.
 */
public interface Observer {
    void onNotificationReceived(String category, String message);
}
