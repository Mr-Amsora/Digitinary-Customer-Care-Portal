package com.example.digitinary_customer_care_portal.dto.ticket;

import com.example.digitinary_customer_care_portal.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketStatusRequest(
        @NotNull TicketStatus status
) {
}
