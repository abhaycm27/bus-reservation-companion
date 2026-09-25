package com.busreservation.model;

/**
 * Driver model inheriting from User. Contains safety ratings, license, and
 * assigned bus.
 */
public class Driver extends User {
    private String licenseNumber;
    private int yearsOfExperience;
    private double safetyRating; // 0.0 to 5.0
    private String assignedBusId;
    private String contactDetails;

    public Driver(String userId, String name, String email, String phone, String authCredentials,
            String licenseNumber, int yearsOfExperience, String contactDetails) {
        super(userId, name, email, phone, authCredentials);

        if (licenseNumber == null || licenseNumber.isBlank()) {
            throw new IllegalArgumentException("License number cannot be empty");
        }
        if (yearsOfExperience < 0) {
            throw new IllegalArgumentException("Years of experience cannot be negative");
        }
        this.licenseNumber = licenseNumber;
        this.yearsOfExperience = yearsOfExperience;
        this.safetyRating = 5.0; // Start with a default perfect safety rating
        this.assignedBusId = null;
        this.contactDetails = contactDetails != null ? contactDetails : phone;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.isBlank())
            throw new IllegalArgumentException("License cannot be empty");
        this.licenseNumber = licenseNumber;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        if (yearsOfExperience < 0)
            throw new IllegalArgumentException("Years of experience cannot be negative");
        this.yearsOfExperience = yearsOfExperience;
    }

    public double getSafetyRating() {
        return safetyRating;
    }

    public void setSafetyRating(double safetyRating) {
        if (safetyRating < 0.0 || safetyRating > 5.0) {
            throw new IllegalArgumentException("Safety rating must be between 0.0 and 5.0");
        }
        this.safetyRating = safetyRating;
    }

    public String getAssignedBusId() {
        return assignedBusId;
    }

    public void setAssignedBusId(String assignedBusId) {
        this.assignedBusId = assignedBusId;
    }

    public String getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(String contactDetails) {
        if (contactDetails == null || contactDetails.isBlank())
            throw new IllegalArgumentException("Contact details cannot be empty");
        this.contactDetails = contactDetails;
    }
}
