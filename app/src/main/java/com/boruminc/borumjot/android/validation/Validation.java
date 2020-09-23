package com.boruminc.borumjot.android.validation;

public abstract class Validation {
    private String email;
    private String password;

    public static String SUCCESS = "Success!"; // Used for equality comparison
    static String INVALID_EMAIL = "Login failed: Invalid email";

    Validation(String em, String pw) {
        email = em;
        password = pw;
    }

    String getEmail() {
        return email;
    }

    public void setEmail(String newEmail) {
        email = newEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String newPassword) {
        password = newPassword;
    }

    public boolean isMissingFields() {
        return email.isEmpty() || password.isEmpty();
    }

    public boolean isEmailValid() {
        return email.contains("@");
    }
}
