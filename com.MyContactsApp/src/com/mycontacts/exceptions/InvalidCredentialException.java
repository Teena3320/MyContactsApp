package com.mycontacts.exceptions;

public class InvalidCredentialException extends Exception {
    public InvalidCredentialsException() {
        super("Invalid email or password.");
    }
}