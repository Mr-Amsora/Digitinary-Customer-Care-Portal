package com.example.digitinary_customer_care_portal.dto.orderItem;

import com.example.digitinary_customer_care_portal.dto.item.ItemDto;

public record OrderItemDto(
        Long id,
        Integer quantity,
        ItemDto item
) {
}