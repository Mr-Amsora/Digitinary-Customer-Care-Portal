package com.example.digitinary_customer_care_portal.dto.ticket;

import com.example.digitinary_customer_care_portal.enums.TicketPriority;
import com.example.digitinary_customer_care_portal.enums.TicketStatus;

import java.util.Date;

public record TicketDto(
        Long id,
        String subject,
        TicketStatus status,
        TicketPriority priority,
        Date createdAt,
        Long customerId
) {
}
