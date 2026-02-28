package com.mycontacts.domain;

public class PersonContact extends Contact {
    public PersonContact(String ownerUserId, String fullName) {
        super(ownerUserId, fullName);
    }

    @Override
    public String getType() { return "Person"; }
}
