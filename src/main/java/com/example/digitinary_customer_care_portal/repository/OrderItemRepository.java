package com.example.digitinary_customer_care_portal.repository;

import com.example.digitinary_customer_care_portal.entity.Order;
import com.example.digitinary_customer_care_portal.entity.OrderItem;
import com.example.digitinary_customer_care_portal.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
