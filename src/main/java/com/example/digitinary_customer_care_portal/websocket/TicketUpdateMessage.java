package com.example.digitinary_customer_care_portal.websocket;

import java.time.Instant;

public record TicketUpdateMessage(
        Long ticketId,
        Long customerId,
        String status,
        Instant updatedAt
) {
}
