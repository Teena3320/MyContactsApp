package com.mycontacts.service;

import com.mycontacts.domain.*;
import com.mycontacts.exceptions.DuplicateContactException;
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.ContactRepository;

import java.util.ArrayList;
import java.util.List;

public class ContactService {
    private final ContactRepository contactRepo;

    public ContactService(ContactRepository contactRepo) {
        this.contactRepo = contactRepo;
    }

    // ===== UC-04: Create (unchanged) =====

    public PersonContact createPerson(String ownerUserId,
                                      String fullName,
                                      List<String> phoneInputs,
                                      List<String> emailInputs)
            throws ValidationException, DuplicateContactException {
        validateOwner(ownerUserId);
        if (fullName == null || fullName.isBlank())
            throw new ValidationException("Name cannot be blank.");

        if (contactRepo.existsByOwnerAndName(ownerUserId, fullName.trim())) {
            throw new DuplicateContactException("A contact with this name already exists.");
        }

        PersonContact contact = new PersonContact(ownerUserId, fullName.trim());
        addPhones(contact, phoneInputs);
        addEmails(contact, emailInputs);

        contactRepo.save(contact);
        return contact;
    }

    public OrganizationContact createOrganization(String ownerUserId,
                                                  String orgName,
                                                  List<String> phoneInputs,
                                                  List<String> emailInputs)
            throws ValidationException, DuplicateContactException {
        validateOwner(ownerUserId);
        if (orgName == null || orgName.isBlank())
            throw new ValidationException("Organization name cannot be blank.");

        if (contactRepo.existsByOwnerAndName(ownerUserId, orgName.trim())) {
            throw new DuplicateContactException("A contact with this name already exists.");
        }

        OrganizationContact contact = new OrganizationContact(ownerUserId, orgName.trim());
        addPhones(contact, phoneInputs);
        addEmails(contact, emailInputs);

        contactRepo.save(contact);
        return contact;
    }

    // ===== UC-06: Edit (unchanged from your last version) =====

    public void updateContactName(String ownerUserId, String contactId, String newName)
            throws ValidationException, DuplicateContactException {
        validateOwner(ownerUserId);
        if (newName == null || newName.isBlank()) throw new ValidationException("Name cannot be blank.");
        Contact c = getOwnedContactOrThrow(ownerUserId, contactId);
        String trimmed = newName.trim();
        if (!c.getName().equalsIgnoreCase(trimmed) &&
            contactRepo.existsByOwnerAndName(ownerUserId, trimmed)) {
            throw new DuplicateContactException("Another contact with this name already exists.");
        }
        c.setName(trimmed);
    }

    public void addPhone(String ownerUserId, String contactId, String phoneRaw) throws ValidationException {
        validateOwner(ownerUserId);
        if (phoneRaw == null || phoneRaw.trim().isEmpty()) throw new ValidationException("Phone cannot be blank.");
        Contact c = getOwnedContactOrThrow(ownerUserId, contactId);
        try { c.addPhone(new PhoneNumber(phoneRaw)); }
        catch (IllegalArgumentException e) { throw new ValidationException("Invalid phone: " + e.getMessage()); }
    }

    public void removePhone(String ownerUserId, String contactId, int index) throws ValidationException {
        validateOwner(ownerUserId);
        Contact c = getOwnedContactOrThrow(ownerUserId, contactId);
        try { c.removePhoneAt(index); }
        catch (IndexOutOfBoundsException e) { throw new ValidationException("Invalid phone index."); }
    }

    public void replacePhones(String ownerUserId, String contactId, List<String> phoneInputs,
                              boolean requireAtLeastOne) throws ValidationException {
        validateOwner(ownerUserId);
        Contact c = getOwnedContactOrThrow(ownerUserId, contactId);
        List<PhoneNumber> list = new ArrayList<>();
        try {
            if (phoneInputs != null) {
                for (String raw : phoneInputs) {
                    if (raw == null || raw.trim().isEmpty()) continue;
                    list.add(new PhoneNumber(raw));
                }
            }
        } catch (IllegalArgumentException e) { throw new ValidationException("Invalid phone: " + e.getMessage()); }
        if (requireAtLeastOne && list.isEmpty()) throw new ValidationException("At least one phone number is required.");
        c.replaceAllPhones(list);
    }

    public void addEmail(String ownerUserId, String contactId, String emailRaw) throws ValidationException {
        validateOwner(ownerUserId);
        if (emailRaw == null || emailRaw.trim().isEmpty()) throw new ValidationException("Email cannot be blank.");
        Contact c = getOwnedContactOrThrow(ownerUserId, contactId);
        try { c.addEmail(new Email(emailRaw)); }
        catch (IllegalArgumentException e) { throw new ValidationException("Invalid email: " + e.getMessage()); }
    }

    public void removeEmail(String ownerUserId, String contactId, int index) throws ValidationException {
        validateOwner(ownerUserId);
        Contact c = getOwnedContactOrThrow(ownerUserId, contactId);
        try { c.removeEmailAt(index); }
        catch (IndexOutOfBoundsException e) { throw new ValidationException("Invalid email index."); }
    }

    public void replaceEmails(String ownerUserId, String contactId, List<String> emailInputs,
                              boolean requireAtLeastOne) throws ValidationException {
        validateOwner(ownerUserId);
        Contact c = getOwnedContactOrThrow(ownerUserId, contactId);
        List<Email> list = new ArrayList<>();
        try {
            if (emailInputs != null) {
                for (String raw : emailInputs) {
                    if (raw == null || raw.trim().isEmpty()) continue;
                    list.add(new Email(raw));
                }
            }
        } catch (IllegalArgumentException e) { throw new ValidationException("Invalid email: " + e.getMessage()); }
        if (requireAtLeastOne && list.isEmpty()) throw new ValidationException("At least one email is required.");
        c.replaceAllEmails(list);
    }

    // ===== UC-07: Delete =====

    /** Soft delete (mark as deleted). */
    public void softDelete(String ownerUserId, String contactId) throws ValidationException {
        validateOwner(ownerUserId);
        Contact c = getOwnedContactOrThrow(ownerUserId, contactId);
        c.softDelete(); // mark deleted on entity
        // persist updated entity state is implicit in memory
    }

    /** Hard delete (permanently remove from store). */
    public void hardDelete(String ownerUserId, String contactId) throws ValidationException {
        validateOwner(ownerUserId);
        boolean ok = contactRepo.hardDelete(ownerUserId, contactId);
        if (!ok) throw new ValidationException("Contact not found or already removed.");
    }

    // ===== helpers =====

    private void addPhones(Contact contact, List<String> phoneInputs) throws ValidationException {
        if (phoneInputs == null) return;
        try {
            for (String raw : phoneInputs) {
                if (raw == null || raw.trim().isEmpty()) continue;
                contact.addPhone(new PhoneNumber(raw));
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid phone: " + e.getMessage());
        }
    }

    private void addEmails(Contact contact, List<String> emailInputs) throws ValidationException {
        if (emailInputs == null) return;
        try {
            for (String raw : emailInputs) {
                if (raw == null || raw.trim().isEmpty()) continue;
                contact.addEmail(new Email(raw));
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid email: " + e.getMessage());
        }
    }

    private void validateOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.isBlank())
            throw new IllegalArgumentException("ownerUserId cannot be blank.");
    }

    private Contact getOwnedContactOrThrow(String ownerUserId, String contactId) throws ValidationException {
        return contactRepo.findById(ownerUserId, contactId)
                .orElseThrow(() -> new ValidationException("Contact not found."));
    }
}