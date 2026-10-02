package com.example.digitinary_customer_care_portal.dto.customer;

import java.util.Date;

public record CustomerDto(
        Long id,
        String name,
        String email,
        String phone,
        Date createdAt
) {
}
