package com.example.digitinary_customer_care_portal.mapping;

import com.example.digitinary_customer_care_portal.dto.item.ItemDto;
import com.example.digitinary_customer_care_portal.entity.Item;

public class ItemToDto {
    public static ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getPrice()
        );
    }
}
