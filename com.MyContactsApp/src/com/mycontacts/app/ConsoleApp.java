/**
 * Use Case 2: User Authentication (Login)
 *
 * This console class handles:
 * - Reading user input (email, password)
 * - Early validation for blank/invalid email (abort attempt)
 * - Verifying credentials via UserService (hash & compare)
 *
 * Demonstrates:
 * - Basic input handling
 * - Exception handling (InvalidCredentialsException)
 * - Simple OOP layering (UI → Service → Repository)
 */

package com.mycontacts.app;

import com.mycontacts.domain.Email;
import com.mycontacts.domain.User;
import com.mycontacts.exceptions.DuplicateEmailException;
import com.mycontacts.exceptions.InvalidCredentialException;
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.UserRepository;
import com.mycontacts.service.UserService;

import java.util.Scanner;

public class ConsoleApp {
    public static void main(String[] args) {
        UserRepository repo = new UserRepository();
        UserService userService = new UserService(repo);

        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;
            System.out.println("=== MyContacts — UC-01 & UC-02 (Register + Login) ===");

            while (running) {
                System.out.println("\nMenu: 1) Register  2) Login  3) List Users  4) Exit");
                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1":
                        handleRegistration(sc, userService);
                        break;

                    case "2":
                        handleLogin(sc, userService);
                        break;

                    case "3":
                        repo.findAll().forEach(System.out::println);
                        break;

                    case "4":
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        }

        System.out.println("Goodbye!");
    }

    private static void handleRegistration(Scanner sc, UserService userService) {
        System.out.println("\n--- Register ---");

        System.out.print("Email: ");
        String email = sc.nextLine();
        try {
            new Email(email); 
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return; 
        }

        System.out.print("Name: ");
        String name = sc.nextLine();

        System.out.print("Password (min 6 chars): ");
        String pass = sc.nextLine();
        if (pass == null || pass.trim().isEmpty()) {
            System.out.println("Registration failed: Password cannot be blank.");
            return; 
        }

        try {
            User u = userService.register(email, name, pass);
            System.out.println("\nRegistered successfully!");
            System.out.println(u);
        } catch (ValidationException | DuplicateEmailException e) {
            System.out.println("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    private static void handleLogin(Scanner sc, UserService userService) {
        System.out.println("\n--- Login ---");

        System.out.print("Email: ");
        String email = sc.nextLine();
        try {
            new Email(email); 
        } catch (IllegalArgumentException e) {
            System.out.println("Login failed: " + e.getMessage());
            return; 
        }

        System.out.print("Password: ");
        String pass = sc.nextLine();

        try {
            User u = userService.login(email, pass);
            System.out.println("\nLogin successful!");
            System.out.println("Welcome, " + u.getName() + " (" + u.getEmail().getValue() + ")");
        } catch (InvalidCredentialException e) {
            System.out.println("Login failed ");
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }
}