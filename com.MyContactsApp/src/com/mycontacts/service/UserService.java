package com.mycontacts.service;

import com.mycontacts.domain.Email;
import com.mycontacts.domain.Password;
import com.mycontacts.domain.User;
import com.mycontacts.exceptions.DuplicateEmailException;
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.UserRepository;
import com.mycontacts.security.PasswordHasher;

public class UserService {
    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    public User register(String emailStr, String name, String rawPassword)
            throws ValidationException, DuplicateEmailException {

        Email email;
        try {
            email = new Email(emailStr);
        } catch (IllegalArgumentException e) {
            throw new ValidationException( e.getMessage());
        }
        if (userRepo.existsByEmail(email)) {
            throw new DuplicateEmailException(email.getValue());
        }

        if (name == null || name.isBlank()) {
            throw new ValidationException("Name cannot be blank.");
        }

        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }


        String hashed = PasswordHasher.sha256(rawPassword);
        Password password = new Password(hashed);

        User user = new User(email, name, password);
        userRepo.save(user);
        return user;
    }
}