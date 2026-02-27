package com.mycontacts.domain;

public class Password {
    private final String hashed;

    public Password(String hashed) {
        if (hashed == null || hashed.isBlank()) {
            throw new IllegalArgumentException("Hashed password cannot be blank.");
        }
        this.hashed = hashed;
    }

    public String getHashed() { return hashed; }
}
