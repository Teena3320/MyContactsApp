package com.mycontacts.view;

import com.mycontacts.domain.Contact;
import com.mycontacts.domain.Email;
import com.mycontacts.domain.PhoneNumber;
import com.mycontacts.domain.Tag;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;


public final class ContactRenderer {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ContactRenderer() {}

    public static String render(Contact c, boolean uppercaseName, boolean maskEmails) {
        String name = c.getName();
        if (uppercaseName && name != null) {
            name = name.toUpperCase();
        }

        String phones = c.getPhones().isEmpty()
                ? "(none)"
                : c.getPhones().stream()
                    .map(PhoneNumber::getDisplay)
                    .collect(Collectors.joining(", "));

        String emails = c.getEmails().isEmpty()
                ? "(none)"
                : c.getEmails().stream()
                    .map(Email::getValue)
                    .map(e -> maskEmails ? maskEmail(e) : e)
                    .collect(Collectors.joining(", "));

        String tags = (c.getTags() == null || c.getTags().isEmpty())
                ? "(none)"
                : c.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.joining(", "));

        String created = c.getCreatedAt() == null ? "-" : DT_FMT.format(c.getCreatedAt());
        String updated = c.getUpdatedAt() == null ? "-" : DT_FMT.format(c.getUpdatedAt());

        return new StringBuilder()
                .append("Contact Details").append(System.lineSeparator())
                .append("----------------").append(System.lineSeparator())
                .append("Type    : ").append(c.getType()).append(System.lineSeparator())
                .append("Name    : ").append(name).append(System.lineSeparator())
                .append("Phones  : ").append(phones).append(System.lineSeparator())
                .append("Emails  : ").append(emails).append(System.lineSeparator())
                .append("Tags    : ").append(tags).append(System.lineSeparator())
                .append("Created : ").append(created).append(System.lineSeparator())
                .append("Updated : ").append(updated).append(System.lineSeparator())
                .toString();
    }

    /** Masks local-part of an email, preserving first & last characters where possible. */
    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0 || at == email.length() - 1) {
            return "****";
        }
        String local = email.substring(0, at);
        String domain = email.substring(at + 1);
        return maskLocal(local) + "@" + domain;
    }

    private static String maskLocal(String local) {
        if (local.length() <= 1) return "*";
        if (local.length() == 2) return local.charAt(0) + "*";
        StringBuilder sb = new StringBuilder();
        sb.append(local.charAt(0));
        for (int i = 0; i < local.length() - 2; i++) sb.append('*');
        sb.append(local.charAt(local.length() - 1));
        return sb.toString();
    }
}
