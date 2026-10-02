package com.example.digitinary_customer_care_portal.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @Column(name = "customer_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false)
    private String name;

    @Column(name = "customer_email", nullable = false, unique = true)
    private String email;

    @Column(name = "customer_phone", nullable = false)
    private String phone;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY,
        mappedBy = "customer"
    )
    private List<Order> orders;

    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY,
        mappedBy = "customer"
    )
    private List<Ticket> tickets;


    public Customer() {
        this.createdAt = new Date();
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }
}