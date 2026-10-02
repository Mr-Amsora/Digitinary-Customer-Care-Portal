package com.example.digitinary_customer_care_portal.mapping;

import com.example.digitinary_customer_care_portal.dto.customer.CustomerDto;
import com.example.digitinary_customer_care_portal.entity.Customer;

public class CustomerToDto {
    public static CustomerDto toDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCreatedAt()
        );
    }
}
