package com.mycontacts.repository;

import com.mycontacts.domain.Email;
import com.mycontacts.domain.User;

import java.util.*;

public class UserRepository {
    private final Map<String, User> byEmail = new HashMap<>();

    public boolean existsByEmail(Email email) {
        return byEmail.containsKey(email.getValue());
    }

    public void save(User user) {
        byEmail.put(user.getEmail().getValue(), user);
    }

    public Optional<User> findByEmail(Email email) {
        return Optional.ofNullable(byEmail.get(email.getValue()));
    }

    public Optional<User> findById(String id) {
        return byEmail.values().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    public Collection<User> findAll() {
        return Collections.unmodifiableCollection(byEmail.values());
    }
}