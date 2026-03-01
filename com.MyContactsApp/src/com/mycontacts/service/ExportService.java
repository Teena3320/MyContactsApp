package com.mycontacts.service;

import com.mycontacts.domain.Contact;
import com.mycontacts.domain.Email;
import com.mycontacts.domain.PhoneNumber;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ExportService {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void exportToCsv(List<Contact> contacts, String filePath) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath))) {
            out.println("Type,Name,Phones,Emails,CreatedAt,UpdatedAt");
            for (Contact c : contacts) {
                String type = c.getType();
                String name = escapeCsv(c.getName());
                String phones = escapeCsv(joinPhones(c));
                String emails = escapeCsv(joinEmails(c));
                String created = c.getCreatedAt() == null ? "" : DT_FMT.format(c.getCreatedAt());
                String updated = c.getUpdatedAt() == null ? "" : DT_FMT.format(c.getUpdatedAt());
                out.printf("%s,%s,%s,%s,%s,%s%n", type, name, phones, emails, created, updated);
            }
        }
    }

    private String joinPhones(Contact c) {
        if (c.getPhones().isEmpty()) return "";
        return c.getPhones().stream().map(PhoneNumber::getDisplay).collect(Collectors.joining("; "));
    }

    private String joinEmails(Contact c) {
        if (c.getEmails().isEmpty()) return "";
        return c.getEmails().stream().map(Email::getValue).collect(Collectors.joining("; "));
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + v + "\"" : v;
    }
}