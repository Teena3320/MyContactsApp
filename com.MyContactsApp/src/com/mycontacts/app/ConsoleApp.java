/** 
 * Use Case 5: View Contact Details
 * 
 *   This module enables:
 *   - Listing all contacts for the logged‑in user
 *   - Selecting a specific contact to view full details
 *   - Displaying formatted information (type, name, phones, emails, timestamps)
 *   Optional enhancements:
 *   - Uppercase contact name
 *   - Masked email addresses
 *   
 *   Demonstrates:
 *   - Read only view rendering
 *   - Clean separation of display logic using ContactRenderer
 *   - Optional formatting flags (uppercase, mask emails)
 *   - Safe access to stored contact data
 *   - Polymorphic behavior (PersonContact / OrganizationContact share display logic)
 */

package com.mycontacts.app;

import com.mycontacts.domain.Contact;
import com.mycontacts.domain.Email;
import com.mycontacts.domain.PhoneNumber;
import com.mycontacts.domain.User;
import com.mycontacts.exceptions.DuplicateContactException;
import com.mycontacts.exceptions.DuplicateEmailException;
import com.mycontacts.exceptions.IncorrectPasswordException;
import com.mycontacts.exceptions.InvalidCredentialException; // singular
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.ContactRepository;
import com.mycontacts.repository.UserRepository;
import com.mycontacts.service.ContactService;
import com.mycontacts.service.UserService;
import com.mycontacts.view.ContactRenderer;

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
            System.out.println("=== MyContacts — UC-01..05 ===");

            while (running) {
                System.out.println("\nMenu:");
                System.out.println(" 1) Register");
                System.out.println(" 2) Login");
                System.out.println(" 3) Profile: Update Name");
                System.out.println(" 4) Profile: Change Password");
                System.out.println(" 5) Contact: Create Person");
                System.out.println(" 6) Contact: Create Organization");
                System.out.println(" 7) View Contacts (names only)");
                System.out.println(" 8) View Contact Details");
                System.out.println(" 9) Logout");
                System.out.println("10) Exit");
                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1": handleRegistration(sc, userService); break;
                    case "2": handleLogin(sc, userService); break;
                    case "3": handleUpdateName(sc, userService); break;
                    case "4": handleChangePassword(sc, userService); break;
                    case "5": handleCreatePerson(sc, contactService); break;
                    case "6": handleCreateOrganization(sc, contactService); break;
                    case "7": handleListMyContactsNamesOnly(contactRepo); break;     
                    case "8": handleViewContactDetails(sc, contactRepo); break;       
                    case "9": currentUser = null; System.out.println("Logged out."); break;
                    case "10": running = false; break;
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
            new Email(email); // early email validation (blank/invalid aborts)
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
            new Email(email); 
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

    private static void handleListMyContactsNamesOnly(ContactRepository contactRepo) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- My Contacts (Names Only) ---");
        var contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) {
            System.out.println("(no contacts yet)");
            return;
        }
        for (int i = 0; i < contacts.size(); i++) {
            var c = contacts.get(i);
            System.out.printf("%d) %s%n", i + 1, c.getName());
        }
    }

    private static void handleViewContactDetails(Scanner sc, ContactRepository contactRepo) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- View Contact Details ---");

        var contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) {
            System.out.println("(no contacts yet)");
            return;
        }

        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%d) [%s] %s%n", i + 1, c.getType(), c.getName());
        }

        int idx = -1;
        while (true) {
            System.out.print("Choose contact number: ");
            String s = sc.nextLine().trim();
            try {
                idx = Integer.parseInt(s);
                if (idx < 1 || idx > contacts.size()) {
                    System.out.println("Please enter a number between 1 and " + contacts.size() + ".");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        Contact chosen = contacts.get(idx - 1);

        boolean uppercase = askYesNo(sc, "Uppercase name? (y/N): ", /* defaultYes= */ false);
        boolean maskEmails = askYesNo(sc, "Mask emails? (Y/n): ", /* defaultYes= */ true);

        String rendered = ContactRenderer.render(chosen, uppercase, maskEmails);
        System.out.println();
        System.out.println(rendered);
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

            if (!askYesNo(sc, "Add another number? (Y/n): ", /* defaultYes= */ true)) {
                if (requireAtLeastOne && phones.isEmpty()) {
                    System.out.println("At least one phone number is required.");
                    continue;
                }
                return phones;
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

            if (!askYesNo(sc, "Add another email? (Y/n): ", /* defaultYes= */ true)) {
                if (requireAtLeastOne && emails.isEmpty()) {
                    System.out.println("At least one email is required.");
                    continue;
                }
                return emails;
            }
        }
    }

    private static boolean askYesNo(Scanner sc, String prompt, boolean defaultYes) {
        while (true) {
            System.out.print(prompt);
            String ans = sc.nextLine();
            if (ans == null || ans.isBlank()) {
                return defaultYes;
            }
            char c = Character.toLowerCase(ans.trim().charAt(0));
            if (c == 'y') return true;
            if (c == 'n') return false;
            System.out.println("Please answer with 'y' or 'n'.");
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