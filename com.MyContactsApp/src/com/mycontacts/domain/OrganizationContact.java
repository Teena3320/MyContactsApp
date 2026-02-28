package com.mycontacts.domain;

public class OrganizationContact extends Contact {
    public OrganizationContact(String ownerUserId, String orgName) {
        super(ownerUserId, orgName);
    }

    @Override
    public String getType() { return "Organization"; }
}