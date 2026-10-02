package com.example.digitinary_customer_care_portal.repository;

import com.example.digitinary_customer_care_portal.entity.Ticket;
import com.example.digitinary_customer_care_portal.enums.TicketPriority;
import com.example.digitinary_customer_care_portal.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("""
           select t from Ticket t
           where t.customer.id = :customerId
             and (:status is null or t.status = :status)
             and (:priority is null or t.priority = :priority)
           """)
    Page<Ticket> search(@Param("customerId") Long customerId,
                        @Param("status") TicketStatus status,
                        @Param("priority") TicketPriority priority,
                        Pageable pageable);
}
