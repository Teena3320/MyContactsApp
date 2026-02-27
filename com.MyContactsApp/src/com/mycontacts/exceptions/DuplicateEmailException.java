package com.mycontacts.exceptions;

public class DuplicateEmailException extends Exception {
    public DuplicateEmailException(String email) {
        super("Email already registered: " + email);
    }
}
``