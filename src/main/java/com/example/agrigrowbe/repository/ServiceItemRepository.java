package com.example.agrigrowbe.repository;

import com.example.agrigrowbe.model.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
}
