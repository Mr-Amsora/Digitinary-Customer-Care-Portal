package com.example.digitinary_customer_care_portal.dto.customer;

import jakarta.validation.constraints.NotBlank;

public record LinkUserRequest(
        @NotBlank String username
) {
}
