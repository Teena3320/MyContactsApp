package com.mycontacts.domain;

import java.util.Objects;
import java.util.UUID;

public class Tag {
    private final String id;
    private final String ownerUserId; 
    private String name;

    public Tag(String ownerUserId, String name) {
        if (ownerUserId == null || ownerUserId.isBlank())
            throw new IllegalArgumentException("ownerUserId cannot be blank.");
        setName(name);
        this.ownerUserId = ownerUserId;
        this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public String getOwnerUserId() { return ownerUserId; }
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Tag name cannot be blank.");
        this.name = name.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // identity-based equality by id
        if (!(o instanceof Tag)) return false;
        Tag other = (Tag) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Tag{id='%s', owner='%s', name='%s'}".formatted(id, ownerUserId, name);
    }
}