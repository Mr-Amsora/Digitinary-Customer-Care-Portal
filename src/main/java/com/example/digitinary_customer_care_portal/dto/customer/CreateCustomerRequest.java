package com.example.digitinary_customer_care_portal.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Date;

public record CreateCustomerRequest(
        @NotBlank
        String name,
        @NotBlank
        @Email(message = "Invalid email format")
        String email,
        @NotBlank
        String phone,
        Date createdAt
) {
}
