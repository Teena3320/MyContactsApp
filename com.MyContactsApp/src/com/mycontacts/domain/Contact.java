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

    // UC-07: soft delete state
    private boolean deleted = false;
    private LocalDateTime deletedAt = null;

    // UC-10: frequently contacted metric
    private int timesContacted = 0;

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

    public boolean isDeleted() { return deleted; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    public int getTimesContacted() { return timesContacted; }

    /** UC-10: mark that user viewed/used this contact. */
    public void markContacted() {
        this.timesContacted++;
        touch();
    }

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

    /** Remove phone by 0-based index. */
    public void removePhoneAt(int index) {
        if (index < 0 || index >= phones.size()) throw new IndexOutOfBoundsException("Invalid phone index.");
        phones.remove(index);
        touch();
    }

    /** Remove email by 0-based index. */
    public void removeEmailAt(int index) {
        if (index < 0 || index >= emails.size()) throw new IndexOutOfBoundsException("Invalid email index.");
        emails.remove(index);
        touch();
    }

    /** Clear and replace all phones with the provided list. */
    public void replaceAllPhones(List<PhoneNumber> newPhones) {
        phones.clear();
        if (newPhones != null) phones.addAll(newPhones);
        touch();
    }

    /** Clear and replace all emails with the provided list. */
    public void replaceAllEmails(List<Email> newEmails) {
        emails.clear();
        if (newEmails != null) emails.addAll(newEmails);
        touch();
    }

    /** UC-07: mark contact as softly deleted. */
    public void softDelete() {
        if (!deleted) {
            deleted = true;
            deletedAt = LocalDateTime.now();
            touch();
        }
    }

    protected void touch() { this.updatedAt = LocalDateTime.now(); }

    /** Contact type string for UI and logging */
    public abstract String getType();

    @Override
    public String toString() {
        return "%sContact{id='%s', owner='%s', name='%s', phones=%s, emails=%s, createdAt=%s, deleted=%s, timesContacted=%d}"
                .formatted(getType().isEmpty() ? "" : (getType() + " "),
                        id, ownerUserId, name, phones, emails, createdAt, deleted, timesContacted);
    }
}