package com.busreservation.model;

/**
 * BusOperator represents the owner/operator company managing fleets of buses.
 */
public class BusOperator extends User {
    private String companyName;

    public BusOperator(String userId, String name, String email, String phone, String authCredentials,
            String companyName) {
        super(userId, name, email, phone, authCredentials);
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        if (companyName == null || companyName.isBlank())
            throw new IllegalArgumentException("Company name cannot be empty");
        this.companyName = companyName;
    }
}
