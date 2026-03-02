/**
 *Use Case 10: Basic Filtering
 *
 *This module enables:
 * - Filtering contacts by tag, date added, or frequency of contact
 * - Displaying a refined list of contacts based on chosen criteria
 * - Supporting sorted results (alphabetical, by creation date) for easier navigation
 * 
 * Input behavior:
 * - User selects a filter type from a menu
 * - Rejects invalid filter choices
 * - Date filters must use non‑blank input (if applicable)
 * - Filters apply only to non‑deleted contacts
 * 
 * Safe filtering flow:
 * - Ensures filters are applied consistently and do not modify contact data
 * - Uses comparison logic safely (dates, strings, tags)
 * - Sorting performed using Java’s built‑in comparators
 * - Prevents errors by ignoring contacts without required metadata (e.g., missing tags)
 * 
 * Demonstrates:
 * - Use of simple filter interfaces or dedicated filter methods
 * - Clean separation between filtering logic and UI display
 * - Encapsulation of comparison logic (alphabetical, date-based)
 * - Java collection utilities:
 * 	Comparator
 * 	Collections.sort()
 *  Loop-based conditional filtering
 *  
 *  Consistent user experience aligned with previous use cases
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
import com.mycontacts.service.ExportService;
import com.mycontacts.service.FilterService; 
import com.mycontacts.service.SearchService; 
import com.mycontacts.service.UserService;
import com.mycontacts.view.ContactRenderer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {

    private static User currentUser = null;

    public static void main(String[] args) {
        UserRepository userRepo = new UserRepository();
        ContactRepository contactRepo = new ContactRepository();
        UserService userService = new UserService(userRepo);
        ContactService contactService = new ContactService(contactRepo);
        ExportService exportService = new ExportService();
        SearchService searchService = new SearchService(contactRepo); 
        FilterService filterService = new FilterService(contactRepo); // UC-10

        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;
            System.out.println("=== MyContacts — UC-10 ===");

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
                System.out.println("11) Bulk Operations");
                System.out.println("12) Logout");
                System.out.println("13) Exit");
                System.out.println("14) Search Contacts");
                System.out.println("15) Filter Contacts"); // UC-10
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
                    case "11": handleBulkOperations(sc, contactRepo, contactService, exportService); break;
                    case "12": currentUser = null; System.out.println("Logged out."); break;
                    case "13": running = false; break;
                    case "14": handleSearchContacts(sc, searchService, contactRepo); break;
                    case "15": handleFilterContacts(sc, filterService); break; // UC-10
                    default: System.out.println("Invalid choice. Try again.");
                }
            }
        }

        System.out.println("Goodbye!");
    }

    // ===== UC-01: Registration =====
    private static void handleRegistration(Scanner sc, UserService userService) {
        System.out.println("\n--- Register ---");

        System.out.print("Email: ");
        String email = sc.nextLine();
        try { new Email(email); } catch (IllegalArgumentException e) { System.out.println("Registration failed: " + e.getMessage()); return; }

        String name = readRequiredNonBlank(sc, "Name: ");

        System.out.print("Password (min 6 chars): ");
        String pass = sc.nextLine();
        if (pass == null || pass.trim().isEmpty()) { System.out.println("Registration failed: Password cannot be blank."); return; }

        try {
            User u = userService.register(email, name, pass);
            System.out.println("\nRegistered successfully!");
            System.out.println(u);
        } catch (ValidationException | DuplicateEmailException e) {
            System.out.println("Registration failed: " + e.getMessage());
        } catch (Exception e) { System.out.println("Unexpected error: " + e.getMessage()); }
    }

    // ===== UC-02: Login =====
    private static void handleLogin(Scanner sc, UserService userService) {
        System.out.println("\n--- Login ---");

        System.out.print("Email: ");
        String email = sc.nextLine();
        try { new Email(email); } catch (IllegalArgumentException e) { System.out.println("Login failed: " + e.getMessage()); return; }

        System.out.print("Password: ");
        String pass = sc.nextLine();

        try {
            currentUser = userService.login(email, pass);
            System.out.println("\nLogin successful!");
            System.out.println("Welcome, " + currentUser.getName() + " (" + currentUser.getEmail().getValue() + ")");
        } catch (InvalidCredentialException e) {
            System.out.println("Login failed: " + e.getMessage());
        } catch (Exception e) { System.out.println("Unexpected error: " + e.getMessage()); }
    }

    // ===== UC-03: Update Name =====
    private static void handleUpdateName(Scanner sc, UserService userService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Update Profile Name ---");
        String newName = readRequiredNonBlank(sc, "New name: ");
        try {
            userService.updateName(currentUser, newName);
            System.out.println("Name updated. Hello, " + currentUser.getName() + "!");
        } catch (ValidationException e) {
            System.out.println("Update failed: " + e.getMessage());
        } catch (Exception e) { System.out.println("Unexpected error: " + e.getMessage()); }
    }

    // ===== UC-03: Change Password =====
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
        } catch (Exception e) { System.out.println("Unexpected error: " + e.getMessage()); }
    }

    // ===== UC-04: Create Person =====
    private static void handleCreatePerson(Scanner sc, ContactService contactService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Create Person Contact ---");
        String name = readRequiredNonBlank(sc, "Full name: ");
        List<String> phones = readPhonesForContact(sc, true);
        List<String> emails = readEmailsForContact(sc, true);
        try {
            Contact person = contactService.createPerson(currentUser.getId(), name, phones, emails);
            System.out.println("\nCreated contact:");
            System.out.println(person);
        } catch (ValidationException | DuplicateContactException e) {
            System.out.println("Creation failed: " + e.getMessage());
        } catch (Exception e) { System.out.println("Unexpected error: " + e.getMessage()); }
    }

    // ===== UC-04: Create Organization =====
    private static void handleCreateOrganization(Scanner sc, ContactService contactService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Create Organization Contact ---");
        String name = readRequiredNonBlank(sc, "Organization name: ");
        List<String> phones = readPhonesForContact(sc, true);
        List<String> emails = readEmailsForContact(sc, true);
        try {
            Contact org = contactService.createOrganization(currentUser.getId(), name, phones, emails);
            System.out.println("\nCreated contact:");
            System.out.println(org);
        } catch (ValidationException | DuplicateContactException e) {
            System.out.println("Creation failed: " + e.getMessage());
        } catch (Exception e) { System.out.println("Unexpected error: " + e.getMessage()); }
    }

    // ===== UC-05: View Contacts (names only) =====
    private static void handleListMyContactsNamesOnly(ContactRepository contactRepo) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- My Contacts (Names Only) ---");
        List<Contact> contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) { System.out.println("(no contacts yet)"); return; }
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%d) %s%n", i + 1, c.getName());
        }
    }

    // ===== UC-05: View Contact Details =====
    private static void handleViewContactDetails(Scanner sc, ContactRepository contactRepo) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- View Contact Details ---");
        List<Contact> contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) { System.out.println("(no contacts yet)"); return; }
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%d) [%s] %s%n", i + 1, c.getType(), c.getName());
        }
        int idx = askIndex(sc, "Choose contact number: ", contacts.size());
        Contact chosen = contacts.get(idx - 1);

        // UC-10: track frequently contacted
        chosen.markContacted();

        boolean uppercase = askYesNo(sc, "Uppercase name? (y/N): ", false);
        boolean maskEmails = askYesNo(sc, "Mask emails? (Y/n): ", true);
        String rendered = com.mycontacts.view.ContactRenderer.render(chosen, uppercase, maskEmails);
        System.out.println("\n" + rendered);
    }

    // ===== UC-06: Edit Contact =====
    private static void handleEditContact(Scanner sc, ContactRepository contactRepo, ContactService contactService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Edit Contact ---");
        List<Contact> contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) { System.out.println("(no contacts yet)"); return; }
        for (int i = 0; i < contacts.size(); i++)
            System.out.printf("%d) [%s] %s%n", i + 1, contacts.get(i).getType(), contacts.get(i).getName());
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
                    case "1": {
                        String newName = readRequiredNonBlank(sc, "New name: ");
                        contactService.updateContactName(ownerId, contactId, newName);
                        System.out.println("Name updated.");
                        break;
                    }
                    case "2": {
                        String phone = readRequiredNonBlank(sc, "Phone to add: ");
                        contactService.addPhone(ownerId, contactId, phone);
                        System.out.println("Phone added.");
                        break;
                    }
                    case "3": {
                        List<PhoneNumber> phones = chosen.getPhones();
                        if (phones.isEmpty()) { System.out.println("No phones to remove."); break; }
                        for (int i = 0; i < phones.size(); i++)
                            System.out.printf("%d) %s%n", i + 1, phones.get(i).getDisplay());
                        int pIdx = askIndex(sc, "Choose phone number to remove: ", phones.size());
                        contactService.removePhone(ownerId, contactId, pIdx - 1);
                        System.out.println("Phone removed.");
                        break;
                    }
                    case "4": {
                        String email = readRequiredNonBlank(sc, "Email to add: ");
                        contactService.addEmail(ownerId, contactId, email);
                        System.out.println("Email added.");
                        break;
                    }
                    case "5": {
                        List<Email> emails = chosen.getEmails();
                        if (emails.isEmpty()) { System.out.println("No emails to remove."); break; }
                        for (int i = 0; i < emails.size(); i++)
                            System.out.printf("%d) %s%n", i + 1, emails.get(i).getValue());
                        int eIdx = askIndex(sc, "Choose email to remove: ", emails.size());
                        contactService.removeEmail(ownerId, contactId, eIdx - 1);
                        System.out.println("Email removed.");
                        break;
                    }
                    case "6": {
                        System.out.println("Enter new phone numbers:");
                        List<String> newPhones = readPhonesForContact(sc, true);
                        contactService.replacePhones(ownerId, contactId, newPhones, true);
                        System.out.println("Phones replaced.");
                        break;
                    }
                    case "7": {
                        System.out.println("Enter new emails:");
                        List<String> newEmails = readEmailsForContact(sc, true);
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
            } catch (Exception e) { System.out.println("Unexpected error: " + e.getMessage()); }
        }
    }

    // ===== UC-07: Delete Contact =====
    private static void handleDeleteContact(Scanner sc, ContactRepository contactRepo, ContactService contactService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Delete Contact ---");
        List<Contact> contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) { System.out.println("(no contacts yet)"); return; }
        for (int i = 0; i < contacts.size(); i++)
            System.out.printf("%d) [%s] %s%n", i + 1, contacts.get(i).getType(), contacts.get(i).getName());
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
                    boolean ok = askYesNo(sc, "Are you sure you want to SOFT delete '" + chosen.getName() + "'? (y/N): ", false);
                    if (!ok) { System.out.println("Cancelled."); return; }
                    contactService.softDelete(ownerId, contactId);
                    contactRepo.findById(ownerId, contactId).ifPresent(c -> {
                        System.out.println("Contact softly deleted.");
                        System.out.println("Deleted flag: " + c.isDeleted());
                        System.out.println("Deleted at  : " + c.getDeletedAt());
                    });
                    break;
                }
                case "2": {
                    boolean ok = askYesNo(sc, "This will PERMANENTLY remove '" + chosen.getName() + "'. Are you sure? (y/N): ", false);
                    if (!ok) { System.out.println("Cancelled."); return; }
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
        } catch (Exception e) { System.out.println("Unexpected error: " + e.getMessage()); }
    }

    // ===== UC-08: Bulk Operations =====
    private static void handleBulkOperations(Scanner sc,
                                             ContactRepository contactRepo,
                                             ContactService contactService,
                                             ExportService exportService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Bulk Operations (UC-08) ---");
        List<Contact> contacts = contactRepo.findAllByOwner(currentUser.getId());
        if (contacts.isEmpty()) { System.out.println("(no contacts yet)"); return; }
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%d) [%s] %s%n", i + 1, c.getType(), c.getName());
        }
        System.out.print("Select contacts (comma-separated numbers, e.g., 1,3,5): ");
        String sel = sc.nextLine().trim();
        List<Integer> indices = parseIndexList(sel, contacts.size());
        if (indices.isEmpty()) { System.out.println("No valid selections."); return; }

        List<String> selectedIds = new ArrayList<>();
        List<Contact> selectedContacts = new ArrayList<>();
        for (int idx : indices) {
            Contact c = contacts.get(idx - 1);
            selectedIds.add(c.getId());
            selectedContacts.add(c);
        }

        System.out.println("\nBulk Operation:");
        System.out.println(" 1) Soft delete selected");
        System.out.println(" 2) Hard delete selected");
        System.out.println(" 3) Export selected to CSV");
        System.out.println(" 4) Cancel");
        System.out.print("Choose: ");
        String op = sc.nextLine().trim();

        try {
            switch (op) {
                case "1": {
                    boolean ok = askYesNo(sc, "Confirm SOFT delete for selected contacts? (y/N): ", false);
                    if (!ok) { System.out.println("Cancelled."); return; }
                    int n = contactService.bulkSoftDelete(currentUser.getId(), selectedIds);
                    System.out.println("Soft-deleted: " + n + " contact(s).");
                    break;
                }
                case "2": {
                    boolean ok = askYesNo(sc, "This will PERMANENTLY remove selected contacts. Proceed? (y/N): ", false);
                    if (!ok) { System.out.println("Cancelled."); return; }
                    int n = contactService.bulkHardDelete(currentUser.getId(), selectedIds);
                    System.out.println("Hard-deleted: " + n + " contact(s).");
                    break;
                }
                case "3": {
                    System.out.print("Enter CSV file path (default: contacts_export.csv): ");
                    String path = sc.nextLine().trim();
                    if (path.isEmpty()) path = "contacts_export.csv";
                    exportService.exportToCsv(selectedContacts, path);
                    System.out.println("Exported " + selectedContacts.size() + " contact(s) to: " + path);
                    break;
                }
                case "4":
                    System.out.println("Cancelled.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        } catch (ValidationException e) {
            System.out.println("Bulk operation failed: " + e.getMessage());
        } catch (java.io.IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    // ===== UC-09: Search Contacts =====
    private static void handleSearchContacts(Scanner sc,
                                             com.mycontacts.service.SearchService searchService,
                                             ContactRepository contactRepo) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Search Contacts (UC-09) ---");
        System.out.println("Tips:");
        System.out.println(" - General:  alice");
        System.out.println(" - By name:  name:alice");
        System.out.println(" - By email: email:@work.com");
        System.out.println(" - By phone: phone:212");
        System.out.print("Enter query: ");
        String q = sc.nextLine();

        try {
            List<Contact> results = searchService.search(currentUser.getId(), q);
            if (results.isEmpty()) { System.out.println("(no matches)"); return; }
            System.out.println("\nMatches:");
            for (int i = 0; i < results.size(); i++) {
                Contact c = results.get(i);
                System.out.printf("%d) [%s] %s%n", i + 1, c.getType(), c.getName());
            }
            boolean view = askYesNo(sc, "View details of one? (Y/n): ", true);
            if (!view) return;
            int idx = askIndex(sc, "Choose match number: ", results.size());
            Contact chosen = results.get(idx - 1);

            // UC-10: track frequently contacted
            chosen.markContacted();

            String rendered = ContactRenderer.render(chosen, false, false);
            System.out.println("\n" + rendered);
        } catch (ValidationException e) {
            System.out.println("Search failed: " + e.getMessage());
        } catch (Exception e) { System.out.println("Unexpected error: " + e.getMessage()); }
    }

    // ===== UC-10: Filter Contacts =====
    private static void handleFilterContacts(Scanner sc, FilterService filterService) {
        if (!ensureLoggedIn()) return;
        System.out.println("\n--- Filter Contacts (UC-10) ---");
        System.out.println(" 1) Filter by Date Added (range)");
        System.out.println(" 2) Frequently Contacted (Top N)");
        System.out.println(" 3) Sort by Name (A→Z)");
        System.out.println(" 4) Sort by Date (Newest first)");
        System.out.println(" 5) Back");
        System.out.print("Choose: ");
        String choice = sc.nextLine().trim();

        try {
            switch (choice) {
                case "1": {
                    LocalDate from = readOptionalDate(sc, "From (YYYY-MM-DD, blank for none): ");
                    LocalDate to   = readOptionalDate(sc, "To   (YYYY-MM-DD, blank for none): ");
                    List<Contact> res = filterService.filterByDateAdded(currentUser.getId(), from, to);
                    printNamesOnly(res);
                    break;
                }
                case "2": {
                    System.out.print("Top N: ");
                    String nStr = sc.nextLine().trim();
                    int n = 5;
                    try { n = Integer.parseInt(nStr); } catch (NumberFormatException ignore) {}
                    if (n < 1) n = 1;
                    List<Contact> res = filterService.topFrequentlyContacted(currentUser.getId(), n);
                    printNamesOnlyWithCount(res);
                    break;
                }
                case "3": {
                    List<Contact> res = filterService.sortByName(currentUser.getId());
                    printNamesOnly(res);
                    break;
                }
                case "4": {
                    List<Contact> res = filterService.sortByCreatedAt(currentUser.getId(), true);
                    printNamesOnlyWithDate(res);
                    break;
                }
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Filter failed: " + e.getMessage());
        }
    }

    // ===== Helpers =====

    private static void printNamesOnly(List<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) { System.out.println("(no results)"); return; }
        for (int i = 0; i < contacts.size(); i++) {
            System.out.printf("%d) %s%n", i + 1, contacts.get(i).getName());
        }
    }

    private static void printNamesOnlyWithDate(List<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) { System.out.println("(no results)"); return; }
        for (int i = 0; i < contacts.size(); i++) {
            var c = contacts.get(i);
            System.out.printf("%d) %s (created: %s)%n", i + 1, c.getName(),
                    c.getCreatedAt() == null ? "-" : c.getCreatedAt().toLocalDate());
        }
    }

    private static void printNamesOnlyWithCount(List<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) { System.out.println("(no results)"); return; }
        for (int i = 0; i < contacts.size(); i++) {
            var c = contacts.get(i);
            System.out.printf("%d) %s (times contacted: %d)%n", i + 1, c.getName(), c.getTimesContacted());
        }
    }

    private static LocalDate readOptionalDate(Scanner sc, String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine().trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            System.out.println("Invalid date. Ignoring.");
            return null;
        }
    }

    private static String readRequiredNonBlank(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String v = sc.nextLine();
            if (v != null && !v.trim().isEmpty()) return v.trim();
            System.out.println("Input cannot be blank. Please try again.");
        }
    }

    private static List<String> readPhonesForContact(Scanner sc, boolean requireAtLeastOne) {
        List<String> phones = new ArrayList<>();
        while (true) {
            System.out.print("Phone: ");
            String raw = sc.nextLine();
            if (raw == null || raw.trim().isEmpty()) { System.out.println("Phone cannot be blank/whitespace. Please enter a valid number."); continue; }
            try { new PhoneNumber(raw); phones.add(raw.trim()); }
            catch (IllegalArgumentException e) { System.out.println("Invalid phone: " + e.getMessage()); continue; }
            if (!askYesNo(sc, "Add another number? (Y/n): ", true)) {
                if (requireAtLeastOne && phones.isEmpty()) { System.out.println("At least one phone number is required."); continue; }
                return phones;
            }
        }
    }

    private static List<String> readEmailsForContact(Scanner sc, boolean requireAtLeastOne) {
        List<String> emails = new ArrayList<>();
        while (true) {
            System.out.print("Email: ");
            String raw = sc.nextLine();
            if (raw == null || raw.trim().isEmpty()) { System.out.println("Email cannot be blank/whitespace. Please enter a valid email."); continue; }
            try { new Email(raw); emails.add(raw.trim()); }
            catch (IllegalArgumentException e) { System.out.println("Invalid email: " + e.getMessage()); continue; }
            if (!askYesNo(sc, "Add another email? (Y/n): ", true)) {
                if (requireAtLeastOne && emails.isEmpty()) { System.out.println("At least one email is required."); continue; }
                return emails;
            }
        }
    }

    private static boolean askYesNo(Scanner sc, String prompt, boolean defaultYes) {
        while (true) {
            System.out.print(prompt);
            String ans = sc.nextLine();
            if (ans == null || ans.isBlank()) return defaultYes;
            char c = Character.toLowerCase(ans.trim().charAt(0));
            if (c == 'y') return true;
            if (c == 'n') return false;
            System.out.println("Please answer with 'y' or 'n'.");
        }
    }

    private static int askIndex(Scanner sc, String prompt, int maxInclusive) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int idx = Integer.parseInt(s);
                if (idx < 1 || idx > maxInclusive) { System.out.println("Please enter a number between 1 and " + maxInclusive + "."); continue; }
                return idx;
            } catch (NumberFormatException e) { System.out.println("Please enter a valid number."); }
        }
    }

    private static List<Integer> parseIndexList(String input, int max) {
        List<Integer> out = new ArrayList<>();
        if (input == null || input.isBlank()) return out;
        String[] parts = input.split(",");
        for (String p : parts) {
            String s = p.trim();
            if (s.isEmpty()) continue;
            try {
                int v = Integer.parseInt(s);
                if (v >= 1 && v <= max) out.add(v);
            } catch (NumberFormatException ignore) {}
        }
        LinkedHashSet<Integer> set = new LinkedHashSet<>(out); // dedupe preserve order
        return new ArrayList<>(set);
    }

    private static boolean ensureLoggedIn() {
        if (currentUser == null) { System.out.println("You must login first."); return false; }
        return true;
    }
}