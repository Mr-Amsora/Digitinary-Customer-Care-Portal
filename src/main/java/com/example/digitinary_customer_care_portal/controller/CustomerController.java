package com.example.digitinary_customer_care_portal.controller;

import com.example.digitinary_customer_care_portal.dto.customer.CreateCustomerRequest;
import com.example.digitinary_customer_care_portal.dto.customer.CustomerDto;
import com.example.digitinary_customer_care_portal.dto.customer.LinkUserRequest;
import com.example.digitinary_customer_care_portal.dto.order.OrderDto;
import com.example.digitinary_customer_care_portal.dto.ticket.TicketDto;
import com.example.digitinary_customer_care_portal.enums.OrderStatus;
import com.example.digitinary_customer_care_portal.enums.TicketPriority;
import com.example.digitinary_customer_care_portal.enums.TicketStatus;
import com.example.digitinary_customer_care_portal.exception.CustomerDoesNotExist;
import com.example.digitinary_customer_care_portal.exception.DuplicateEmail;
import com.example.digitinary_customer_care_portal.service.CustomerService;
import com.example.digitinary_customer_care_portal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final UserService userService;

    public CustomerController(CustomerService customerService, UserService userService) {
        this.customerService = customerService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id, Authentication auth) {
        userService.checkOwner(auth, id);
        return ResponseEntity.ok(customerService.get(id));
    }

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(
            @Valid @RequestBody CreateCustomerRequest createCustomerRequest, Authentication auth) {
        CustomerDto createdCustomer = customerService.create(createCustomerRequest, auth);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCustomer.id())
                .toUri();

        return ResponseEntity.created(location).body(createdCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> replaceCustomer(
            @PathVariable Long id, @Valid @RequestBody CreateCustomerRequest createCustomerRequest) {
        return ResponseEntity.ok(customerService.replace(id, createCustomerRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeCustomer(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomerId(
            @PathVariable Long id,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth
    ) {
        userService.checkOwner(auth, id);
        return ResponseEntity.ok(customerService.getOrders(id, status, sort, page, size));
    }

    @GetMapping("/{id}/tickets")
    public ResponseEntity<List<TicketDto>> getTicketsByCustomerId(
            @PathVariable Long id,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth
    ) {
        userService.checkOwner(auth, id);
        return ResponseEntity.ok(customerService.getTickets(id, status, priority, sort, page, size));
    }

    @PutMapping("/{id}/user")
    public ResponseEntity<Void> linkUser(
            @PathVariable Long id,
            @Valid @RequestBody LinkUserRequest req) {
        customerService.linkUser(id, req.username());
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(DuplicateEmail.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmailException(DuplicateEmail ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CustomerDoesNotExist.class)
    public ResponseEntity<Map<String, String>> handleCustomerDoesNotExistException(CustomerDoesNotExist ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(Map.of("error", errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Void> handleDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}