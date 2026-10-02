package com.example.digitinary_customer_care_portal.mapping;

import com.example.digitinary_customer_care_portal.dto.ticket.TicketDto;
import com.example.digitinary_customer_care_portal.entity.Ticket;

import java.util.Date;

public class TicketToDto {
    public static TicketDto toDto(Ticket ticket) {
        return new TicketDto(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt(),
                ticket.getCustomer().getId()
        );
    }
}
