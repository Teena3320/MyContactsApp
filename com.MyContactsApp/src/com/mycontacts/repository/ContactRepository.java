package com.mycontacts.repository;

import com.mycontacts.domain.Contact;

import java.util.*;
import java.util.stream.Collectors;

public class ContactRepository {
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

    public List<Contact> findAllByOwner(String ownerUserId) {
        Map<String, Contact> byId = store.get(ownerUserId);
        if (byId == null) return List.of();
        return byId.values().stream()
                .sorted(Comparator.comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public boolean existsByOwnerAndName(String ownerUserId, String name) {
        return findAllByOwner(ownerUserId).stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));
    }
}