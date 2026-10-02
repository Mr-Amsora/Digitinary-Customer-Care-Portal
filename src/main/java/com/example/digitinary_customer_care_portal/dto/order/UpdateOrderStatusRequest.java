package com.example.digitinary_customer_care_portal.dto.order;

import com.example.digitinary_customer_care_portal.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status
) {
}
