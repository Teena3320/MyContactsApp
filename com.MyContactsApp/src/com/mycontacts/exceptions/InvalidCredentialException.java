package com.mycontacts.exceptions;

public class InvalidCredentialException extends Exception {
    public InvalidCredentialException() {
        super("Invalid email or password.");
    }
}