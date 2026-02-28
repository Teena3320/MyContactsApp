package com.mycontacts.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class Contact {
    private final String id;
    private final String ownerUserId;    
    private String name;                  
    private final List<PhoneNumber> phones = new ArrayList<>();
    private final List<Email> emails = new ArrayList<>();
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Contact(String ownerUserId, String name) {
        if (ownerUserId == null || ownerUserId.isBlank()) {
            throw new IllegalArgumentException("ownerUserId cannot be blank.");
        }
        setName(name);
        this.ownerUserId = ownerUserId;
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() { return id; }
    public String getOwnerUserId() { return ownerUserId; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be blank.");
        this.name = name.trim();
        touch();
    }

    public List<PhoneNumber> getPhones() { return Collections.unmodifiableList(phones); }
    public List<Email> getEmails() { return Collections.unmodifiableList(emails); }

    public void addPhone(PhoneNumber p) {
        if (p == null) throw new IllegalArgumentException("Phone number cannot be null.");
        phones.add(p);
        touch();
    }

    public void addEmail(Email e) {
        if (e == null) throw new IllegalArgumentException("Email cannot be null.");
        emails.add(e);
        touch();
    }

    protected void touch() { this.updatedAt = LocalDateTime.now(); }

    public abstract String getType();

    @Override
    public String toString() {
        return "%sContact{id='%s', owner='%s', name='%s', phones=%s, emails=%s, createdAt=%s}"
                .formatted(getType().isEmpty() ? "" : (getType() + " "),
                        id, ownerUserId, name, phones, emails, createdAt);
    }
}