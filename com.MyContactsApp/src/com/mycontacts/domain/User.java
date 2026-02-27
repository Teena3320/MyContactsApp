package com.mycontacts.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class User {
    private final String id;
    private final Email email;
    private String name;
    private Password password; 
    private final LocalDateTime createdAt;

    public User(Email email, String name, Password password) {
        if (email == null) throw new IllegalArgumentException("Email cannot be null.");
        if (password == null) throw new IllegalArgumentException("Password cannot be null.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be blank.");
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.name = name.trim();
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public Email getEmail() { return email; }
    public String getName() { return name; }
    public Password getPassword() { return password; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be blank.");
        this.name = name.trim();
    }

    public void setPassword(Password newPassword) {
        if (newPassword == null) throw new IllegalArgumentException("Password cannot be null.");
        this.password = newPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return email.equals(other.email); 
    }

    @Override
    public int hashCode() { return Objects.hash(email); }

    @Override
    public String toString() {
        return "User{id='%s', email='%s', name='%s', createdAt=%s}"
            .formatted(id, email.getValue(), name, createdAt);
    }
}
