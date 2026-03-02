package com.mycontacts.service;

import com.mycontacts.domain.Contact;
import com.mycontacts.domain.Tag;
import com.mycontacts.exceptions.DuplicateTagException;
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.ContactRepository;
import com.mycontacts.repository.TagRepository;

import java.util.List;
import java.util.Optional;

public class TagService {
    private final TagRepository tagRepo;
    private final ContactRepository contactRepo;

    public TagService(TagRepository tagRepo, ContactRepository contactRepo) {
        this.tagRepo = tagRepo;
        this.contactRepo = contactRepo;
    }

    public Tag createTag(String ownerUserId, String name)
            throws ValidationException, DuplicateTagException {
        validateOwner(ownerUserId);
        if (name == null || name.isBlank())
            throw new ValidationException("Tag name cannot be blank.");
        String trimmed = name.trim();

        if (tagRepo.existsByName(ownerUserId, trimmed))
            throw new DuplicateTagException(trimmed);

        Tag tag = new Tag(ownerUserId, trimmed);
        tagRepo.save(tag);
        return tag;
    }

    public void renameTag(String ownerUserId, String tagId, String newName)
            throws ValidationException, DuplicateTagException {
        validateOwner(ownerUserId);
        if (newName == null || newName.isBlank())
            throw new ValidationException("Tag name cannot be blank.");
        String trimmed = newName.trim();

        Tag t = tagRepo.findById(ownerUserId, tagId)
                .orElseThrow(() -> new ValidationException("Tag not found."));

        if (!t.getName().equalsIgnoreCase(trimmed) && tagRepo.existsByName(ownerUserId, trimmed))
            throw new DuplicateTagException(trimmed);

        t.setName(trimmed);
        tagRepo.save(t);
    }

    public void deleteTag(String ownerUserId, String tagId) throws ValidationException {
        validateOwner(ownerUserId);
        Tag t = tagRepo.findById(ownerUserId, tagId)
                .orElseThrow(() -> new ValidationException("Tag not found."));

        // Remove from contacts
        List<Contact> contacts = contactRepo.findAllByOwner(ownerUserId);
        for (Contact c : contacts) {
            if (c.hasTagId(tagId)) {
                c.removeTagById(tagId);
            }
        }

        // Remove tag
        boolean ok = tagRepo.deleteById(ownerUserId, tagId);
        if (!ok) throw new ValidationException("Tag could not be deleted.");
    }

    public List<Tag> listTags(String ownerUserId) {
        validateOwner(ownerUserId);
        return tagRepo.findAllByOwner(ownerUserId);
    }

    public void addTagToContact(String ownerUserId, String contactId, String tagId) throws ValidationException {
        validateOwner(ownerUserId);
        Contact c = contactRepo.findById(ownerUserId, contactId)
                .orElseThrow(() -> new ValidationException("Contact not found."));
        Tag t = tagRepo.findById(ownerUserId, tagId)
                .orElseThrow(() -> new ValidationException("Tag not found."));
        c.addTag(t);
    }

    public void removeTagFromContact(String ownerUserId, String contactId, String tagId) throws ValidationException {
        validateOwner(ownerUserId);
        Contact c = contactRepo.findById(ownerUserId, contactId)
                .orElseThrow(() -> new ValidationException("Contact not found."));
        c.removeTagById(tagId);
    }

    private void validateOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.isBlank())
            throw new IllegalArgumentException("ownerUserId cannot be blank.");
    }
}