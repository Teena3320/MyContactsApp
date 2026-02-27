package com.mycontacts.service;

import com.mycontacts.domain.Email;
import com.mycontacts.domain.Password;
import com.mycontacts.domain.User;
import com.mycontacts.exceptions.DuplicateEmailException;
import com.mycontacts.exceptions.InvalidCredentialException;
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.UserRepository;
import com.mycontacts.security.PasswordHasher;

import java.util.Optional;

public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User register(String emailStr, String name, String rawPassword)
            throws ValidationException, DuplicateEmailException {

        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }

        Email email;
        try {
            email = new Email(emailStr);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid email: " + e.getMessage());
        }

        if (name == null || name.isBlank()) {
            throw new ValidationException("Name cannot be blank.");
        }

        if (userRepo.existsByEmail(email)) {
            throw new DuplicateEmailException(email.getValue());
        }

        String hashed = PasswordHasher.sha256(rawPassword);
        Password password = new Password(hashed);

        User user = new User(email, name, password);
        userRepo.save(user);
        return user;
    }
    public User login(String emailStr, String rawPassword) throws InvalidCredentialException {
        Email email;
        try {
            email = new Email(emailStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialException();
        }

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialException();
        }

        User user = userOpt.get();
        String hashedInput = PasswordHasher.sha256(rawPassword);
        if (!user.getPassword().getHashed().equals(hashedInput)) {
            throw new InvalidCredentialException();
        }

        return user;
    }
} 