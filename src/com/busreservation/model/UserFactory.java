package com.busreservation.model;

/**
 * Factory for creating different User hierarchy objects.
 */
public class UserFactory {

    public static Passenger createPassenger(String userId, String name, String email, String phone,
            String authCredentials) {
        return new Passenger(userId, name, email, phone, authCredentials);
    }

    public static Driver createDriver(String userId, String name, String email, String phone, String authCredentials,
            String licenseNumber, int yearsOfExperience, String contactDetails) {
        return new Driver(userId, name, email, phone, authCredentials, licenseNumber, yearsOfExperience,
                contactDetails);
    }

    public static Admin createAdmin(String userId, String name, String email, String phone, String authCredentials,
            String adminLevel) {
        return new Admin(userId, name, email, phone, authCredentials, adminLevel);
    }

    public static BusOperator createBusOperator(String userId, String name, String email, String phone,
            String authCredentials, String companyName) {
        return new BusOperator(userId, name, email, phone, authCredentials, companyName);
    }
}
