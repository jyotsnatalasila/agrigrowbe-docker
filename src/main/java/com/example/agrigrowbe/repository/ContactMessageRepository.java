package com.example.agrigrowbe.repository;

import com.example.agrigrowbe.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
