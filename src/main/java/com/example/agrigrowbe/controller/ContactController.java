package com.example.agrigrowbe.controller;

import com.example.agrigrowbe.model.ContactMessage;
import com.example.agrigrowbe.repository.ContactMessageRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "http://localhost:4000")
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;

    public ContactController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @PostMapping
    public ContactMessage submitContactMessage(@RequestBody ContactMessage contactMessage){
        return contactMessageRepository.save(contactMessage);
    }
}
