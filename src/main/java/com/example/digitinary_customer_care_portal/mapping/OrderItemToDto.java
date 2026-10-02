package com.example.digitinary_customer_care_portal.mapping;

import com.example.digitinary_customer_care_portal.dto.orderItem.OrderItemDto;
import com.example.digitinary_customer_care_portal.entity.OrderItem;

public class OrderItemToDto {
    public static OrderItemDto toDto(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getId(),
                orderItem.getQuantity(),
                ItemToDto.toDto(orderItem.getItem())
        );
    }
}
