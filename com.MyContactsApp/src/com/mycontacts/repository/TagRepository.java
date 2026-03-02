package com.mycontacts.repository;

import com.mycontacts.domain.Tag;

import java.util.*;
import java.util.stream.Collectors;

public class TagRepository {
    private final Map<String, Map<String, Tag>> store = new HashMap<>();

    public void save(Tag tag) {
        store.computeIfAbsent(tag.getOwnerUserId(), k -> new HashMap<>())
             .put(tag.getId(), tag);
    }

    public Optional<Tag> findById(String ownerUserId, String tagId) {
        Map<String, Tag> byId = store.get(ownerUserId);
        if (byId == null) return Optional.empty();
        return Optional.ofNullable(byId.get(tagId));
    }

    public Optional<Tag> findByName(String ownerUserId, String name) {
        Map<String, Tag> byId = store.get(ownerUserId);
        if (byId == null) return Optional.empty();
        for (Tag t : byId.values()) {
            if (t.getName().equalsIgnoreCase(name)) return Optional.of(t);
        }
        return Optional.empty();
    }

    public boolean existsByName(String ownerUserId, String name) {
        return findByName(ownerUserId, name).isPresent();
    }

    public List<Tag> findAllByOwner(String ownerUserId) {
        Map<String, Tag> byId = store.get(ownerUserId);
        if (byId == null) return List.of();
        return byId.values().stream()
                .sorted(Comparator.comparing(Tag::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public boolean deleteById(String ownerUserId, String tagId) {
        Map<String, Tag> byId = store.get(ownerUserId);
        if (byId == null) return false;
        Tag removed = byId.remove(tagId);
        if (byId.isEmpty()) store.remove(ownerUserId);
        return removed != null;
    }
}