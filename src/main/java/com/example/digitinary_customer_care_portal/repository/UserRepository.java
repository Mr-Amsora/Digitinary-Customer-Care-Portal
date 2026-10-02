package com.example.digitinary_customer_care_portal.repository;

import com.example.digitinary_customer_care_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByCustomer_Id(Long customerId);
}
