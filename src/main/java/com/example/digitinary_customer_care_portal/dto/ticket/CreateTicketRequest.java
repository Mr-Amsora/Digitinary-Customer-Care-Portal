package com.example.digitinary_customer_care_portal.dto.ticket;

import com.example.digitinary_customer_care_portal.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTicketRequest(
        @NotBlank String subject,
        @NotNull TicketPriority priority,
        Long customerId
) {
}
