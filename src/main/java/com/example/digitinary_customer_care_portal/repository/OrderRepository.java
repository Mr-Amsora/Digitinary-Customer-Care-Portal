package com.example.digitinary_customer_care_portal.repository;

import com.example.digitinary_customer_care_portal.entity.Order;
import com.example.digitinary_customer_care_portal.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
           select o from Order o
           where o.customer.id = :customerId
             and (:status is null or o.status = :status)
           """)
    Page<Order> search(@Param("customerId") Long customerId,
                       @Param("status") OrderStatus status,
                       Pageable pageable);
}
