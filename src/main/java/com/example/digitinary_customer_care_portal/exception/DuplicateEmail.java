package com.example.digitinary_customer_care_portal.exception;

public class DuplicateEmail extends RuntimeException {
    public DuplicateEmail(String message) {
        super(message);
    }
}
