package com.mycontacts.domain;

import java.time.LocalDateTime;
import java.util.*;

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

    // UC-11: Tags attached to this contact (unique by Tag.id)
    private final Set<Tag> tags = new LinkedHashSet<>();

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
    public void markContacted() { this.timesContacted++; touch(); }

    public Set<Tag> getTags() { return Collections.unmodifiableSet(tags); }

    public void addTag(Tag tag) {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null.");
        if (!Objects.equals(tag.getOwnerUserId(), this.ownerUserId)) {
            throw new IllegalArgumentException("Cannot add a tag from a different owner.");
        }
        if (tags.add(tag)) touch();
    }

    public void removeTag(Tag tag) {
        if (tag == null) return;
        if (tags.remove(tag)) touch();
    }

    public boolean hasTagId(String tagId) {
        for (Tag t : tags) if (t.getId().equals(tagId)) return true;
        return false;
    }

    public void removeTagById(String tagId) {
        if (tagId == null) return;
        boolean changed = tags.removeIf(t -> t.getId().equals(tagId));
        if (changed) touch();
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

    public void removePhoneAt(int index) {
        if (index < 0 || index >= phones.size()) throw new IndexOutOfBoundsException("Invalid phone index.");
        phones.remove(index);
        touch();
    }

    public void removeEmailAt(int index) {
        if (index < 0 || index >= emails.size()) throw new IndexOutOfBoundsException("Invalid email index.");
        emails.remove(index);
        touch();
    }

    public void replaceAllPhones(List<PhoneNumber> newPhones) {
        phones.clear();
        if (newPhones != null) phones.addAll(newPhones);
        touch();
    }

    public void replaceAllEmails(List<Email> newEmails) {
        emails.clear();
        if (newEmails != null) emails.addAll(newEmails);
        touch();
    }

    public void softDelete() {
        if (!deleted) {
            deleted = true;
            deletedAt = LocalDateTime.now();
            touch();
        }
    }

    protected void touch() { this.updatedAt = LocalDateTime.now(); }

    public abstract String getType();

    @Override
    public String toString() {
        return "%sContact{id='%s', owner='%s', name='%s', phones=%s, emails=%s, createdAt=%s, deleted=%s, timesContacted=%d, tags=%s}"
                .formatted(getType().isEmpty() ? "" : (getType() + " "),
                        id, ownerUserId, name, phones, emails, createdAt, deleted, timesContacted, tagNames());
    }

    private String tagNames() {
        if (tags.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Tag t : tags) {
            if (!first) sb.append(", ");
            sb.append(t.getName());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}