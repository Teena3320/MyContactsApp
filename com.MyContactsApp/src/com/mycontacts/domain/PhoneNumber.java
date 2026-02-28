package com.mycontacts.domain;

import java.util.Objects;

public class PhoneNumber {
    private final String value;      
    private final String display;    
    
    public PhoneNumber(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be blank.");
        }
        String trimmed = raw.trim();

        String normalized = trimmed.replaceAll("[\\s-]", "");
        if (!normalized.matches("^\\+?\\d{7,15}$")) {
            throw new IllegalArgumentException("Invalid phone number format.");
        }

        this.value = normalized;
        this.display = trimmed;
    }

    public String getValue() { return value; }

    public String getDisplay() { return display; }

    @Override
    public String toString() { return display; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber)) return false;
        PhoneNumber that = (PhoneNumber) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}