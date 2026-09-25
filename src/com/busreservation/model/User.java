package com.busreservation.model;

/**
 * Abstract Base Class representing a User on the platform.
 */
public abstract class User {
    private final String userId;
    private String name;
    private String email;
    private String phone;
    private String authCredentials;

    public User(String userId, String name, String email, String phone, String authCredentials) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be empty or null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty or null");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address format");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty or null");
        }
        if (authCredentials == null || authCredentials.isBlank()) {
            throw new IllegalArgumentException("Auth credentials cannot be empty or null");
        }

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.authCredentials = authCredentials;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("Invalid email format");
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) throw new IllegalArgumentException("Phone cannot be empty");
        this.phone = phone;
    }

    public String getAuthCredentials() {
        return authCredentials;
    }

    public void setAuthCredentials(String authCredentials) {
        if (authCredentials == null || authCredentials.isBlank()) throw new IllegalArgumentException("Credentials cannot be empty");
        this.authCredentials = authCredentials;
    }
}
