package com.example.digitinary_customer_care_portal.exception;

public class CustomerDoesNotExist extends RuntimeException {
    public CustomerDoesNotExist(String message) {
        super(message);
    }
}
