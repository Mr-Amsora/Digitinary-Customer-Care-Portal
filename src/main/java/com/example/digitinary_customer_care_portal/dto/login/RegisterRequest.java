package com.example.digitinary_customer_care_portal.dto.login;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank String username, @NotBlank String password) {}