/** 
 * Use Case 7: Delete Contact
 * 
 * Supports two deletion modes:
 * - Soft Delete
 * - Marks contact as “deleted”
 * Hidden from:
 * - View names
 * - View details
 * 
 * Data preserved internally (recoverable if needed)
 * Hard Delete
 * Permanently removes the contact from the repository
 * 
 * UI flow:
 * - Choose a contact
 * Choose delete type:
 * 1) Soft Delete
 * 2) Hard Delete
 * 3) Cancel
 * 
 * Demonstrates:
 * - Entity lifecycle management
 * - Soft vs hard deletion
 * - Repository update logic
 * - Exception handling for invalid deletions
 * 
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
            System.out.println("=== MyContacts — UC-07 ===");

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
                System.out.println(" 9) Edit Contact");
                System.out.println("10) Delete Contact");
                System.out.println("11) Logout");
                System.out.println("12) Exit");
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
                    case "9": handleEditContact(sc, contactRepo, contactService); break;
                    case "10": handleDeleteContact(sc, contactRepo, contactService); break;
                    case "11": currentUser = null; System.out.println("Logged out."); break;
                    case "12": running = false; break;
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
            Contact person = contactService.createPerson(currentUser.getId(), name, phones, emails);
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
            Contact org = contactService.createOrganization(currentUser.getId(), name, phones, emails);
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
        List<Contact> contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) {
            System.out.println("(no contacts yet)");
            return;
        }
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%d) %s%n", i + 1, c.getName());
        }
    }

    private static void handleViewContactDetails(Scanner sc, ContactRepository contactRepo) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- View Contact Details ---");

        List<Contact> contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) {
            System.out.println("(no contacts yet)");
            return;
        }

        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%d) [%s] %s%n", i + 1, c.getType(), c.getName());
        }

        int idx = askIndex(sc, "Choose contact number: ", contacts.size());
        Contact chosen = contacts.get(idx - 1);

        boolean uppercase = askYesNo(sc, "Uppercase name? (y/N): ", /* defaultYes= */ false);
        boolean maskEmails = askYesNo(sc, "Mask emails? (Y/n): ", /* defaultYes= */ true);

        String rendered = ContactRenderer.render(chosen, uppercase, maskEmails);
        System.out.println();
        System.out.println(rendered);
    }

    private static void handleEditContact(Scanner sc, ContactRepository contactRepo, ContactService contactService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Edit Contact ---");

        List<Contact> contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) {
            System.out.println("(no contacts yet)");
            return;
        }

        // List with indices
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%d) [%s] %s%n", i + 1, c.getType(), c.getName());
        }

        int idx = askIndex(sc, "Choose contact number to edit: ", contacts.size());
        Contact chosen = contacts.get(idx - 1);
        String contactId = chosen.getId();
        String ownerId = currentUser.getId();

        boolean editing = true;
        while (editing) {
            System.out.println("\nEdit Menu for: " + chosen.getName());
            System.out.println(" 1) Change name");
            System.out.println(" 2) Add phone");
            System.out.println(" 3) Remove phone");
            System.out.println(" 4) Add email");
            System.out.println(" 5) Remove email");
            System.out.println(" 6) Replace all phones");
            System.out.println(" 7) Replace all emails");
            System.out.println(" 8) Back");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1": { // Change name
                        String newName = readRequiredNonBlank(sc, "New name: ");
                        contactService.updateContactName(ownerId, contactId, newName);
                        System.out.println("Name updated.");
                        break;
                    }
                    case "2": { // Add phone
                        String phone = readRequiredNonBlank(sc, "Phone to add: ");
                        contactService.addPhone(ownerId, contactId, phone);
                        System.out.println("Phone added.");
                        break;
                    }
                    case "3": { // Remove phone
                        List<PhoneNumber> phones = chosen.getPhones();
                        if (phones.isEmpty()) {
                            System.out.println("No phones to remove.");
                            break;
                        }
                        for (int i = 0; i < phones.size(); i++) {
                            System.out.printf("%d) %s%n", i + 1, phones.get(i).getDisplay());
                        }
                        int pIdx = askIndex(sc, "Choose phone number to remove: ", phones.size());
                        contactService.removePhone(ownerId, contactId, pIdx - 1);
                        System.out.println("Phone removed.");
                        break;
                    }
                    case "4": { // Add email
                        String email = readRequiredNonBlank(sc, "Email to add: ");
                        contactService.addEmail(ownerId, contactId, email);
                        System.out.println("Email added.");
                        break;
                    }
                    case "5": { // Remove email
                        List<Email> emails = chosen.getEmails();
                        if (emails.isEmpty()) {
                            System.out.println("No emails to remove.");
                            break;
                        }
                        for (int i = 0; i < emails.size(); i++) {
                            System.out.printf("%d) %s%n", i + 1, emails.get(i).getValue());
                        }
                        int eIdx = askIndex(sc, "Choose email to remove: ", emails.size());
                        contactService.removeEmail(ownerId, contactId, eIdx - 1);
                        System.out.println("Email removed.");
                        break;
                    }
                    case "6": { // Replace all phones (at least one required)
                        System.out.println("Enter new phone numbers:");
                        List<String> newPhones = readPhonesForContact(sc, /* requireAtLeastOne = */ true);
                        contactService.replacePhones(ownerId, contactId, newPhones, true);
                        System.out.println("Phones replaced.");
                        break;
                    }
                    case "7": { // Replace all emails (at least one required)
                        System.out.println("Enter new emails:");
                        List<String> newEmails = readEmailsForContact(sc, /* requireAtLeastOne = */ true);
                        contactService.replaceEmails(ownerId, contactId, newEmails, true);
                        System.out.println("Emails replaced.");
                        break;
                    }
                    case "8":
                        editing = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (DuplicateContactException | ValidationException e) {
                System.out.println("Edit failed: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
    }

    // ===== UC-07: Delete Contact =====
    private static void handleDeleteContact(Scanner sc, ContactRepository contactRepo, ContactService contactService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Delete Contact ---");

        List<Contact> contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) {
            System.out.println("(no contacts yet)");
            return;
        }

        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%d) [%s] %s%n", i + 1, c.getType(), c.getName());
        }

        int idx = askIndex(sc, "Choose contact number to delete: ", contacts.size());
        Contact chosen = contacts.get(idx - 1);
        String ownerId = currentUser.getId();
        String contactId = chosen.getId();

        System.out.println("\nDelete Type:");
        System.out.println(" 1) Soft delete (hide contact, can be kept internally)");
        System.out.println(" 2) Hard delete (permanent removal)");
        System.out.println(" 3) Cancel");
        System.out.print("Choose: ");
        String deleteChoice = sc.nextLine().trim();

        try {
            switch (deleteChoice) {
                case "1": {
                    boolean ok = askYesNo(sc,
                            "Are you sure you want to SOFT delete '" + chosen.getName() + "'? (y/N): ",
                            /* defaultYes= */ false);
                    if (!ok) { System.out.println("Cancelled."); return; }
                    // Soft delete: mark entity as deleted (hidden from lists)
                    contactService.softDelete(ownerId, contactId);
                    System.out.println("Contact softly deleted.");
                    break;
                }
                case "2": {
                    boolean ok = askYesNo(sc,
                            "This will PERMANENTLY remove '" + chosen.getName() + "'. Are you sure? (y/N): ",
                            /* defaultYes= */ false);
                    if (!ok) { System.out.println("Cancelled."); return; }
                    // Hard delete: remove from repository
                    contactService.hardDelete(ownerId, contactId);
                    System.out.println("Contact permanently removed.");
                    break;
                }
                case "3":
                    System.out.println("Cancelled.");
                    return;
                default:
                    System.out.println("Invalid choice. Cancelled.");
            }
        } catch (ValidationException e) {
            System.out.println("Delete failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    // ===== Helpers =====

    // Re-prompt until a non-blank string is entered; trims before returning.
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

    /**
     * Collect one or more valid phone numbers for a contact.
     * - Validates each entry immediately using PhoneNumber value object.
     * - Rejects whitespace-only input.
     * - After each valid phone, asks "Add another number? (Y/n): "
     *   • If user enters 'n' or 'N' → finishes.
     *   • Any other input (including Enter) → continue asking.
     * - If requireAtLeastOne == true, keeps asking until at least one valid phone is provided.
     */
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

    /**
     * Collect one or more valid emails for a contact.
     * - Validates each entry immediately using Email value object.
     * - Rejects whitespace-only input.
     * - After each valid email, asks "Add another email? (Y/n): "
     *   • If user enters 'n' or 'N' → finishes.
     *   • Any other input (including Enter) → continue asking.
     * - If requireAtLeastOne == true, keeps asking until at least one valid email is provided.
     */
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

    /** Asks a Y/n question; defaultYes controls Enter behavior. Returns true for Yes. */
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

    /** Ask for a 1..N index with validation. */
    private static int askIndex(Scanner sc, String prompt, int maxInclusive) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int idx = Integer.parseInt(s);
                if (idx < 1 || idx > maxInclusive) {
                    System.out.println("Please enter a number between 1 and " + maxInclusive + ".");
                    continue;
                }
                return idx;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
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