package com.example.digitinary_customer_care_portal.entity;

import com.example.digitinary_customer_care_portal.enums.TicketPriority;
import com.example.digitinary_customer_care_portal.enums.TicketStatus;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @Column(name = "priority", nullable = false)
    private TicketPriority priority;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public Ticket() {
        this.createdAt = new Date();
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getSubject() {
        return subject;
    }
}