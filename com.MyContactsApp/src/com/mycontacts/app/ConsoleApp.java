/**
 * Use Case 3: User Profile Management
 *
 * This console class enables:
 * - Updating profile name
 * - Changing password (with current-password verification)
 *
 * Demonstrates:
 * - Encapsulation (User mutators with validation)
 * - Exception handling (Validation & IncorrectPassword)
 * - Simple in-memory state (current logged-in user)
 */

package com.mycontacts.app;

import com.mycontacts.domain.Email;
import com.mycontacts.domain.User;
import com.mycontacts.exceptions.DuplicateEmailException;
import com.mycontacts.exceptions.IncorrectPasswordException;
import com.mycontacts.exceptions.InvalidCredentialException;
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.UserRepository;
import com.mycontacts.service.UserService;

import java.util.Scanner;

public class ConsoleApp {

	private static User currentUser = null; 

	public static void main(String[] args) {
		UserRepository repo = new UserRepository();
		UserService userService = new UserService(repo);

		try (Scanner sc = new Scanner(System.in)) {
			boolean running = true;
			System.out.println("=== MyContacts — UC-01/02/03 ===");

			while (running) {
				System.out.println("\nMenu:");
				System.out.println(" 1) Register");
				System.out.println(" 2) Login");
				System.out.println(" 3) Profile: Update Name");
				System.out.println(" 4) Profile: Change Password");
				System.out.println(" 5) List Users (debug)");
				System.out.println(" 6) Logout");
				System.out.println(" 7) Exit");
				System.out.print("Choose: ");

				String choice = sc.nextLine().trim();
				switch (choice) {
				case "1": handleRegistration(sc, userService); break;
				case "2": handleLogin(sc, userService); break;
				case "3": handleUpdateName(sc, userService); break;
				case "4": handleChangePassword(sc, userService); break;
				case "5": repo.findAll().forEach(System.out::println); break;
				case "6": currentUser = null; System.out.println("Logged out."); break;
				case "7": running = false; break;
				default: System.out.println("Invalid choice. Try again.");
				}
			}
		}

		System.out.println("Goodbye!");
	}

	private static void handleRegistration(Scanner sc, UserService userService) {
		System.out.println("\n--- Register ---");

		System.out.print("Email: ");
		String email = sc.nextLine();
		try { new Email(email); }
		catch (IllegalArgumentException e) { System.out.println("Registration failed: " + e.getMessage()); return; }

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
		try { new Email(email); }
		catch (IllegalArgumentException e) { System.out.println("Login failed: " + e.getMessage()); return; }

		System.out.print("Password: ");
		String pass = sc.nextLine();

		try {
			currentUser = userService.login(email, pass);
			System.out.println("\nLogin successful!");
			System.out.println("Welcome, " + currentUser.getName() + " (" + currentUser.getEmail().getValue() + ")");
		} catch (InvalidCredentialException e) {
			System.out.println("Login failed: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Unexpected error: " + e.getMessage());
		}
	}

	private static void handleUpdateName(Scanner sc, UserService userService) {
		if (!ensureLoggedIn()) return;

		System.out.println("\n--- Update Profile Name ---");
		System.out.print("New name: ");
		String newName = sc.nextLine();

		try {
			userService.updateName(currentUser, newName);
			System.out.println("Name updated. Hello, " + currentUser.getName() + "!");
		} catch (ValidationException e) {
			System.out.println("Update failed: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Unexpected error: " + e.getMessage());
		}
	}

	private static void handleChangePassword(Scanner sc, UserService userService) {
		if (!ensureLoggedIn()) return;

		System.out.println("\n--- Change Password ---");
		System.out.print("Current password: ");
		String current = sc.nextLine();
		System.out.print("New password (min 6 chars): ");
		String newPass = sc.nextLine();

		try {
			userService.changePassword(currentUser, current, newPass);
			System.out.println("Password changed successfully.");
		} catch (IncorrectPasswordException | ValidationException e) {
			System.out.println("Change password failed: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Unexpected error: " + e.getMessage());
		}
	}

	private static boolean ensureLoggedIn() {
		if (currentUser == null) {
			System.out.println("You must login first.");
			return false;
		}
		return true;
	}
}
