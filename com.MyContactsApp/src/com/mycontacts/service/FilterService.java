package com.mycontacts.service;

import com.mycontacts.domain.Contact;
import com.mycontacts.repository.ContactRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FilterService {

    private final ContactRepository contactRepo;

    public FilterService(ContactRepository contactRepo) {
        this.contactRepo = contactRepo;
    }

    public List<Contact> filterByDateAdded(String ownerUserId, LocalDate fromInclusive, LocalDate toInclusive) {
        List<Contact> all = contactRepo.findAllByOwner(ownerUserId);
        return all.stream()
                .filter(c -> {
                    LocalDate d = c.getCreatedAt() == null ? null : c.getCreatedAt().toLocalDate();
                    if (d == null) return false;
                    boolean okFrom = (fromInclusive == null) || (!d.isBefore(fromInclusive));
                    boolean okTo   = (toInclusive == null) || (!d.isAfter(toInclusive));
                    return okFrom && okTo;
                })
                .sorted(Comparator.comparing(Contact::getCreatedAt)) // oldest first by default
                .collect(Collectors.toList());
    }

    public List<Contact> topFrequentlyContacted(String ownerUserId, int topN) {
        List<Contact> all = contactRepo.findAllByOwner(ownerUserId);
        return all.stream()
                .sorted(Comparator
                        .comparingInt(Contact::getTimesContacted).reversed()
                        .thenComparing(Contact::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(Math.max(0, topN))
                .collect(Collectors.toList());
    }

    public List<Contact> sortByName(String ownerUserId) {
        List<Contact> all = contactRepo.findAllByOwner(ownerUserId);
        return all.stream()
                .sorted(Comparator.comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<Contact> sortByCreatedAt(String ownerUserId, boolean newestFirst) {
        List<Contact> all = contactRepo.findAllByOwner(ownerUserId);
        Comparator<Contact> cmp = Comparator.comparing(Contact::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        if (newestFirst) cmp = cmp.reversed();
        return all.stream().sorted(cmp).collect(Collectors.toList());
    }
}