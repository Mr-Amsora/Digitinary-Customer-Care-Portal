package com.example.digitinary_customer_care_portal.controller;

import com.example.digitinary_customer_care_portal.dto.order.UpdateOrderStatusRequest;
import com.example.digitinary_customer_care_portal.entity.Order;
import com.example.digitinary_customer_care_portal.enums.OrderStatus;
import com.example.digitinary_customer_care_portal.repository.OrderRepository;
import com.example.digitinary_customer_care_portal.service.UserService;
import com.example.digitinary_customer_care_portal.webhook.OrderStatusChangedEvent;
import com.example.digitinary_customer_care_portal.webhook.WebhookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final WebhookService webhookService;

    public OrderController(OrderRepository orderRepository,
                           UserService userService,
                           WebhookService webhookService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.webhookService = webhookService;
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest req,
            Authentication auth) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order with id " + orderId + " not found"));

        if (!userService.isAdmin(auth)) {
            userService.checkOwner(auth, order.getCustomer().getId());
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(req.status());
        orderRepository.save(order);

        webhookService.deliver("order.status_changed", new OrderStatusChangedEvent(
                "order.status_changed",
                orderId,
                previousStatus.name(),
                req.status().name(),
                Instant.now()
        ));

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
}
