package com.mycontacts.service;

import com.mycontacts.domain.Contact;
import com.mycontacts.domain.Email;
import com.mycontacts.domain.PhoneNumber;
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.ContactRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchService {

    private final ContactRepository contactRepo;
    private static final Pattern FIELD_QUERY =
            Pattern.compile("^(?i)(name|email|phone)\\s*:\\s*(.+)$");

    public SearchService(ContactRepository contactRepo) {
        this.contactRepo = contactRepo;
    }

    public List<Contact> search(String ownerUserId, String query) throws ValidationException {
        if (ownerUserId == null || ownerUserId.isBlank()) {
            throw new IllegalArgumentException("ownerUserId cannot be blank.");
        }
        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException("Search query cannot be blank.");
        }

        String trimmed = query.trim();
        List<Contact> all = contactRepo.findAllByOwner(ownerUserId);
        Matcher m = FIELD_QUERY.matcher(trimmed);

        if (m.matches()) {
            String field = m.group(1).toLowerCase(Locale.ROOT);
            String value = m.group(2).trim();
            return switch (field) {
                case "name"  -> searchByName(all, value);
                case "email" -> searchByEmail(all, value);
                case "phone" -> searchByPhone(all, value);
                default      -> generalSearch(all, trimmed);
            };
        } else {
            return generalSearch(all, trimmed);
        }
    }

    private List<Contact> generalSearch(List<Contact> contacts, String text) {
        String q = text.toLowerCase(Locale.ROOT);
        List<Contact> out = new ArrayList<>();
        for (Contact c : contacts) {
            if (matchesName(c, q) || matchesAnyEmail(c, q) || matchesAnyPhone(c, q)) {
                out.add(c);
            }
        }
        return out;
    }

    private List<Contact> searchByName(List<Contact> contacts, String namePart) {
        String q = namePart.toLowerCase(Locale.ROOT);
        List<Contact> out = new ArrayList<>();
        for (Contact c : contacts) {
            if (matchesName(c, q)) out.add(c);
        }
        return out;
    }

    private List<Contact> searchByEmail(List<Contact> contacts, String emailPart) {
        String q = emailPart.toLowerCase(Locale.ROOT);
        List<Contact> out = new ArrayList<>();
        for (Contact c : contacts) {
            if (matchesAnyEmail(c, q)) out.add(c);
        }
        return out;
    }

    private List<Contact> searchByPhone(List<Contact> contacts, String phonePart) {
        String qDigits = normalizeDigits(phonePart);
        String qLower = phonePart.toLowerCase(Locale.ROOT);
        List<Contact> out = new ArrayList<>();
        for (Contact c : contacts) {
            if (matchesAnyPhone(c, qLower, qDigits)) out.add(c);
        }
        return out;
    }

    // ----- match helpers -----

    private boolean matchesName(Contact c, String qLower) {
        return c.getName() != null && c.getName().toLowerCase(Locale.ROOT).contains(qLower);
    }

    private boolean matchesAnyEmail(Contact c, String qLower) {
        if (c.getEmails() == null || c.getEmails().isEmpty()) return false;
        for (Email e : c.getEmails()) {
            String val = e.getValue();
            if (val != null && val.toLowerCase(Locale.ROOT).contains(qLower)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesAnyPhone(Contact c, String qLower) {
        return matchesAnyPhone(c, qLower, normalizeDigits(qLower));
    }

    private boolean matchesAnyPhone(Contact c, String qLower, String qDigits) {
        if (c.getPhones() == null || c.getPhones().isEmpty()) return false;
        for (PhoneNumber p : c.getPhones()) {
            String disp = p.getDisplay();
            String norm = p.getValue(); // normalized stored form
            if ((disp != null && disp.toLowerCase(Locale.ROOT).contains(qLower))
                    || (norm != null && normalizeDigits(norm).contains(qDigits))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeDigits(String s) {
        if (s == null) return "";
        return s.replaceAll("[^0-9]", "");
    }
}