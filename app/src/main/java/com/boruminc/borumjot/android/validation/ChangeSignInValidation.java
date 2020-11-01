package com.boruminc.borumjot.android.validation;

public class ChangeSignInValidation extends Validation {
    private String confirmPassword;

    private final static String PASSWORD_NOT_CONFIRMED = "The passwords do not match";

    ChangeSignInValidation(String em, String pw, String cpw) {
        super(em, pw);
        confirmPassword = cpw;
    }

    private boolean isPasswordConfirmed() {
        return getPassword().equals(confirmPassword);
    }

    public String validate() {
        if (!isPasswordConfirmed()) return PASSWORD_NOT_CONFIRMED;

        return SUCCESS;
    }
}
