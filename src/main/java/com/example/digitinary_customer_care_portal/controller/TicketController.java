package com.example.digitinary_customer_care_portal.controller;

import com.example.digitinary_customer_care_portal.dto.ticket.CreateTicketRequest;
import com.example.digitinary_customer_care_portal.dto.ticket.TicketDto;
import com.example.digitinary_customer_care_portal.dto.ticket.UpdateTicketStatusRequest;
import com.example.digitinary_customer_care_portal.entity.Customer;
import com.example.digitinary_customer_care_portal.entity.Ticket;
import com.example.digitinary_customer_care_portal.enums.TicketStatus;
import com.example.digitinary_customer_care_portal.mapping.TicketToDto;
import com.example.digitinary_customer_care_portal.repository.CustomerRepository;
import com.example.digitinary_customer_care_portal.repository.TicketRepository;
import com.example.digitinary_customer_care_portal.service.UserService;
import com.example.digitinary_customer_care_portal.websocket.TicketUpdateMessage;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public TicketController(TicketRepository ticketRepository,
                            CustomerRepository customerRepository,
                            UserService userService,
                            SimpMessagingTemplate messagingTemplate) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<TicketDto> createTicket(
            @Valid @RequestBody CreateTicketRequest req,
            Authentication auth) {

        Long customerId;

        if (userService.isAdmin(auth)) {
            if (req.customerId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "customerId is required for ADMIN");
            }
            customerId = req.customerId();
        } else {
            customerId = userService.getCustomerOrThrow(auth).getId();
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Customer with id " + customerId + " not found"));

        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setSubject(req.subject());
        ticket.setPriority(req.priority());
        ticket.setStatus(TicketStatus.OPEN);
        ticketRepository.save(ticket);

        broadcast(ticket);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(ticket.getId())
                .toUri();

        return ResponseEntity.created(location).body(TicketToDto.toDto(ticket));
    }

    @PutMapping("/{ticketId}/status")
    public ResponseEntity<TicketDto> updateTicketStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketStatusRequest req,
            Authentication auth) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ticket with id " + ticketId + " not found"));

        if (!userService.isAdmin(auth)) {
            userService.checkOwner(auth, ticket.getCustomer().getId());
        }

        ticket.setStatus(req.status());
        ticketRepository.save(ticket);

        broadcast(ticket);

        return ResponseEntity.ok(TicketToDto.toDto(ticket));
    }

    private void broadcast(Ticket ticket) {
        messagingTemplate.convertAndSend(
                "/topic/tickets",
                new TicketUpdateMessage(
                        ticket.getId(),
                        ticket.getCustomer().getId(),
                        ticket.getStatus().name(),
                        Instant.now()
                )
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
}
