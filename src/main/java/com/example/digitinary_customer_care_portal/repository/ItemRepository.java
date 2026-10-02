package com.example.digitinary_customer_care_portal.repository;

import com.example.digitinary_customer_care_portal.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
