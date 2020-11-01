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

    void setEmail(String newEmail) {
        email = newEmail;
    }

    String getPassword() {
        return password;
    }

    void setPassword(String newPassword) {
        password = newPassword;
    }

    boolean isMissingFields() {
        return email.isEmpty() || password.isEmpty();
    }

    boolean isEmailNotValid() {
        return !email.contains("@");
    }

    abstract String validate();
}
