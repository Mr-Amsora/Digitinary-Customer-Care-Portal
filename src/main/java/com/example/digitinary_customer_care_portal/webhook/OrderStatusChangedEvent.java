package com.example.digitinary_customer_care_portal.webhook;

import java.time.Instant;

public record OrderStatusChangedEvent(
        String event,
        Long orderId,
        String previousStatus,
        String newStatus,
        Instant occurredAt
) {
}
