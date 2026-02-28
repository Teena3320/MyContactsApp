package com.mycontacts.repository;

import com.mycontacts.domain.Contact;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory contact store grouped by owner user ID.
 */
public class ContactRepository {
    // ownerUserId -> (contactId -> Contact)
    private final Map<String, Map<String, Contact>> store = new HashMap<>();

    public void save(Contact c) {
        store.computeIfAbsent(c.getOwnerUserId(), k -> new HashMap<>())
             .put(c.getId(), c);
    }

    public Optional<Contact> findById(String ownerUserId, String contactId) {
        Map<String, Contact> byId = store.get(ownerUserId);
        if (byId == null) return Optional.empty();
        return Optional.ofNullable(byId.get(contactId));
    }

    /** Returns only non-deleted contacts for the owner, sorted by name (case-insensitive). */
    public List<Contact> findAllByOwner(String ownerUserId) {
        Map<String, Contact> byId = store.get(ownerUserId);
        if (byId == null) return List.of();
        return byId.values().stream()
                .filter(c -> !c.isDeleted())
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList());
    }

    /** Checks if a (non-deleted) contact with the given name exists for the owner. */
    public boolean existsByOwnerAndName(String ownerUserId, String name) {
        Map<String, Contact> byId = store.get(ownerUserId);
        if (byId == null) return false;
        return byId.values().stream()
                .filter(c -> !c.isDeleted())
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));
    }

    /** UC-07: Soft delete (mark as deleted) — returns true if updated. */
    public boolean softDelete(String ownerUserId, String contactId) {
        Map<String, Contact> byId = store.get(ownerUserId);
        if (byId == null) return false;
        Contact c = byId.get(contactId);
        if (c == null) return false;
        c.softDelete();
        return true;
    }

    /** UC-07: Hard delete (permanently remove) — returns true if removed. */
    public boolean hardDelete(String ownerUserId, String contactId) {
        Map<String, Contact> byId = store.get(ownerUserId);
        if (byId == null) return false;
        Contact removed = byId.remove(contactId);
        if (byId.isEmpty()) {
            store.remove(ownerUserId); // tidy up empty map
        }
        return removed != null;
    }
}