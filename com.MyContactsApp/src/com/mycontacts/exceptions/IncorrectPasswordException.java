package com.mycontacts.exceptions;

public class IncorrectPasswordException extends Exception {
    public IncorrectPasswordException() {
        super("Current password is incorrect.");
    }
}
