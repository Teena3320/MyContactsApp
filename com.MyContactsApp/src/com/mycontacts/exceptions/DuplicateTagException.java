package com.mycontacts.exceptions;

public class DuplicateTagException extends Exception {
    public DuplicateTagException(String name) {
        super("Tag already exists with name: " + name);
    }
}