package com.mycontacts.service;

import com.mycontacts.domain.*;
import com.mycontacts.exceptions.DuplicateContactException;
import com.mycontacts.exceptions.ValidationException;
import com.mycontacts.repository.ContactRepository;

import java.util.List;

public class ContactService {
	private final ContactRepository contactRepo;

	public ContactService(ContactRepository contactRepo) {
		this.contactRepo = contactRepo;
	}
	public PersonContact createPerson(String ownerUserId,
			String fullName,
			List<String> phoneInputs,
			List<String> emailInputs)
					throws ValidationException, DuplicateContactException {

		if (ownerUserId == null || ownerUserId.isBlank())
			throw new IllegalArgumentException("ownerUserId cannot be blank.");
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

	/**
	 * UC-04: Create an Organization contact for the specified owner.
	 */
	public OrganizationContact createOrganization(String ownerUserId,
			String orgName,
			List<String> phoneInputs,
			List<String> emailInputs)
					throws ValidationException, DuplicateContactException {

		if (ownerUserId == null || ownerUserId.isBlank())
			throw new IllegalArgumentException("ownerUserId cannot be blank.");
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
}