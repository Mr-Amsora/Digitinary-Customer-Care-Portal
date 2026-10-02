package com.example.digitinary_customer_care_portal.dto.order;

import com.example.digitinary_customer_care_portal.dto.orderItem.OrderItemDto;
import com.example.digitinary_customer_care_portal.enums.OrderStatus;

import java.util.Date;
import java.util.Set;

public record OrderDto(
        Long id,
        OrderStatus status,
        Double total,
        Date createdAt,
        Long customerId,
        Set<OrderItemDto> orderItems
) {
}
