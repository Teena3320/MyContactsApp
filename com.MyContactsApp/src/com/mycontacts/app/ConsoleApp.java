package com.mycontacts.app;

import com.mycontacts.domain.Email;
import com.mycontacts.domain.User;
import com.mycontacts.exceptions.DuplicateEmailException;
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.UserRepository;
import com.mycontacts.service.UserService;

import java.util.Scanner;

public class ConsoleApp {
    public static void main(String[] args) {
        UserRepository repo = new UserRepository();
        UserService userService = new UserService(repo);

        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("=== MyContacts — UC-01: User Registration ===");

            while (true) {
                try {
                    System.out.print("Email: ");
                    String email = sc.nextLine();

                    try {
                        new Email(email);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Registration failed: " + e.getMessage());
                        break; 
                    }

                    System.out.print("Name: ");
                    String name = sc.nextLine();

                    System.out.print("Password (min 6 chars): ");
                    String pass = sc.nextLine();

                    User u = userService.register(email, name, pass);
                    System.out.println("\nRegistered successfully!");
                    System.out.println(u);

                    break;

                } catch (ValidationException | DuplicateEmailException e) {
                    System.out.println("Registration failed: " + e.getMessage());
              
                    break; 
                } catch (Exception e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                    break;
                }
            }
        }
    }
}