package com.busreservation.model;

/**
 * Admin user for administrative actions and system setup.
 */
public class Admin extends User {
    private String adminLevel; // e.g. SUPER_ADMIN, REGIONAL_ADMIN

    public Admin(String userId, String name, String email, String phone, String authCredentials, String adminLevel) {
        super(userId, name, email, phone, authCredentials);
        if (adminLevel == null || adminLevel.isBlank()) {
            throw new IllegalArgumentException("Admin level cannot be empty");
        }
        this.adminLevel = adminLevel;
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        if (adminLevel == null || adminLevel.isBlank())
            throw new IllegalArgumentException("Admin level cannot be empty");
        this.adminLevel = adminLevel;
    }
}
