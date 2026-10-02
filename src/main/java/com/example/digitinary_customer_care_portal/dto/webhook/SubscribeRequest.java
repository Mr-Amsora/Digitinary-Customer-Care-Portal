package com.example.digitinary_customer_care_portal.dto.webhook;

import jakarta.validation.constraints.NotBlank;

public record SubscribeRequest(
        @NotBlank String url,
        @NotBlank String eventType
) {
}
