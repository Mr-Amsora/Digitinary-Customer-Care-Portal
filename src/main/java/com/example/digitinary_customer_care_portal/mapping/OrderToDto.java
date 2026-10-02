package com.example.digitinary_customer_care_portal.mapping;

import com.example.digitinary_customer_care_portal.dto.order.OrderDto;
import com.example.digitinary_customer_care_portal.dto.orderItem.OrderItemDto;
import com.example.digitinary_customer_care_portal.entity.Order;

import java.util.HashSet;

public class OrderToDto {
    public static OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getCustomer().getId(),
                new HashSet<OrderItemDto>(order.getOrderItems().stream().map(OrderItemToDto::toDto).toList())
        );
    }
}
