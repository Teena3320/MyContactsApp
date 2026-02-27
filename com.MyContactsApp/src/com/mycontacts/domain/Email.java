package com.mycontacts.domain;

import java.util.regex.Pattern;

public class Email {
    private static final Pattern EMAIL_REGEX = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE
    );

    private final String value;

    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank.");
        }
        String trimmed = value.trim();
        if (!EMAIL_REGEX.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.value = trimmed.toLowerCase();
    }

    public String getValue() { return value; }

    @Override
    public String toString() { return value; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Email)) return false;
        Email other = (Email) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() { return value.hashCode(); }
}