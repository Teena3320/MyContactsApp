/**
 * Use Case 4: Create Contact
 *
 * This module enables:
 * - Creating Person or Organization contacts
 * - Adding multiple phone numbers and emails with validation
 *
 * Demonstrates:
 * - Inheritance (Contact → Person/Organization)
 * - Composition (Contact has PhoneNumber, Email)
 * - Exception handling (Validation, Duplicate contact)
 */

package com.mycontacts.app;

import com.mycontacts.domain.Contact;
import com.mycontacts.domain.Email;
import com.mycontacts.domain.PhoneNumber;
import com.mycontacts.domain.User;
import com.mycontacts.exceptions.DuplicateContactException;
import com.mycontacts.exceptions.DuplicateEmailException;
import com.mycontacts.exceptions.IncorrectPasswordException;
import com.mycontacts.exceptions.InvalidCredentialException;
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.ContactRepository;
import com.mycontacts.repository.UserRepository;
import com.mycontacts.service.ContactService;
import com.mycontacts.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {

    private static User currentUser = null;

    public static void main(String[] args) {
        UserRepository userRepo = new UserRepository();
        ContactRepository contactRepo = new ContactRepository();
        UserService userService = new UserService(userRepo);
        ContactService contactService = new ContactService(contactRepo);

        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;
            System.out.println("=== MyContacts — UC-04 ===");

            while (running) {
                System.out.println("\nMenu:");
                System.out.println(" 1) Register");
                System.out.println(" 2) Login");
                System.out.println(" 3) Profile: Update Name");
                System.out.println(" 4) Profile: Change Password");
                System.out.println(" 5) Contact: Create Person");
                System.out.println(" 6) Contact: Create Organization");
                System.out.println(" 7) List My Contacts");
                System.out.println(" 8) Logout");
                System.out.println(" 9) Exit");
                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1": handleRegistration(sc, userService); break;
                    case "2": handleLogin(sc, userService); break;
                    case "3": handleUpdateName(sc, userService); break;
                    case "4": handleChangePassword(sc, userService); break;
                    case "5": handleCreatePerson(sc, contactService); break;
                    case "6": handleCreateOrganization(sc, contactService); break;
                    case "7": handleListMyContacts(contactRepo); break;
                    case "8": currentUser = null; System.out.println("Logged out."); break;
                    case "9": running = false; break;
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
        try {
            new Email(email); 
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return;
        }

        String name = readRequiredNonBlank(sc, "Name: ");

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
            new Email(email); // early email validation
        } catch (IllegalArgumentException e) {
            System.out.println("Login failed: " + e.getMessage());
            return;
        }

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

        String newName = readRequiredNonBlank(sc, "New name: ");
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

    private static void handleCreatePerson(Scanner sc, ContactService contactService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Create Person Contact ---");

        String name = readRequiredNonBlank(sc, "Full name: ");

        List<String> phones = readPhonesForContact(sc, /* requireAtLeastOne = */ true);
        List<String> emails = readEmailsForContact(sc, /* requireAtLeastOne = */ true);

        try {
            var person = contactService.createPerson(currentUser.getId(), name, phones, emails);
            System.out.println("\nCreated contact:");
            System.out.println(person);
        } catch (ValidationException | DuplicateContactException e) {
            System.out.println("Creation failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    private static void handleCreateOrganization(Scanner sc, ContactService contactService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Create Organization Contact ---");

        String name = readRequiredNonBlank(sc, "Organization name: ");

        List<String> phones = readPhonesForContact(sc, /* requireAtLeastOne = */ true);
        List<String> emails = readEmailsForContact(sc, /* requireAtLeastOne = */ true);

        try {
            var org = contactService.createOrganization(currentUser.getId(), name, phones, emails);
            System.out.println("\nCreated contact:");
            System.out.println(org);
        } catch (ValidationException | DuplicateContactException e) {
            System.out.println("Creation failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    private static void handleListMyContacts(ContactRepository contactRepo) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- My Contacts ---");
        var contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) {
            System.out.println("(no contacts yet)");
            return;
        }
        for (Contact c : contacts) System.out.println(c);
    }

    private static String readRequiredNonBlank(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String v = sc.nextLine();
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
            System.out.println("Input cannot be blank. Please try again.");
        }
    }

    private static List<String> readPhonesForContact(Scanner sc, boolean requireAtLeastOne) {
        List<String> phones = new ArrayList<>();
        while (true) {
            System.out.print("Phone: ");
            String raw = sc.nextLine();

            if (raw == null || raw.trim().isEmpty()) {
                System.out.println("Phone cannot be blank/whitespace. Please enter a valid number.");
                continue;
            }

            try {
                new PhoneNumber(raw); 
                phones.add(raw.trim());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid phone: " + e.getMessage());
                continue;
            }

            while (true) {
                System.out.print("Add another number? (Y/n): ");
                String ans = sc.nextLine();
                if (ans == null || ans.isBlank()) {
                    // Default = yes (keep adding)
                    break; 
                }
                char c = ans.trim().charAt(0);
                if (c == 'n' || c == 'N') {
                    if (requireAtLeastOne && phones.isEmpty()) {
                        System.out.println("At least one phone number is required.");
                        break;
                    }
                    return phones;
                } else if (c == 'y' || c == 'Y') {
                    break; 
                } else {
                    System.out.println("Please answer with 'Y' to add more or 'n' to stop.");
                }
            }
        }
    }

    private static List<String> readEmailsForContact(Scanner sc, boolean requireAtLeastOne) {
        List<String> emails = new ArrayList<>();
        while (true) {
            System.out.print("Email: ");
            String raw = sc.nextLine();

            if (raw == null || raw.trim().isEmpty()) {
                System.out.println("Email cannot be blank/whitespace. Please enter a valid email.");
                continue;
            }

            try {
                new Email(raw); 
                emails.add(raw.trim());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid email: " + e.getMessage());
                continue;
            }

            while (true) {
                System.out.print("Add another email? (Y/n): ");
                String ans = sc.nextLine();
                if (ans == null || ans.isBlank()) {
                    break; 
                }
                char c = ans.trim().charAt(0);
                if (c == 'n' || c == 'N') {
                    if (requireAtLeastOne && emails.isEmpty()) {
                        System.out.println("At least one email is required.");
                        break;
                    }
                    return emails;
                } else if (c == 'y' || c == 'Y') {
                    break; 
                } else {
                    System.out.println("Please answer with 'Y' to add more or 'n' to stop.");
                }
            }
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
