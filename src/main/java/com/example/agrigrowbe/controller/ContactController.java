package com.example.agrigrowbe.controller;

import com.example.agrigrowbe.model.ContactMessage;
import com.example.agrigrowbe.repository.ContactMessageRepository;
import com.example.agrigrowbe.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = {"http://ec2-13-48-31-208.eu-north-1.compute.amazonaws.com:4000", "http://ec2-13-48-31-208.eu-north-1.compute.amazonaws.com:8080"}) // ✅ UPDATED
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;

    @Value("${app.email}")
    private String siteEmail;

    public ContactController(ContactMessageRepository contactMessageRepository, EmailService emailService) {
        this.contactMessageRepository = contactMessageRepository;
        this.emailService = emailService;
    }

    @PostMapping
    public Map<String, Object> submitContactMessage(@RequestBody ContactMessage contactMessage) {
        logger.info("=== CONTACT FORM SUBMISSION ===");
        logger.info("From: {} <{}>", contactMessage.getName(), contactMessage.getEmail());
        logger.info("Subject: {}", contactMessage.getSubject());
        logger.info("Site email configured: {}", siteEmail);

        ContactMessage saved = contactMessageRepository.save(contactMessage);
        logger.info("Message saved to database with ID: {}", saved.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("saved", true);
        result.put("id", saved.getId());

        boolean adminEmailSent = false;
        boolean userAckSent = false;

        try {
            // 1. Send notification to ADMIN (jyotsnatalasila@gmail.com)
            if (siteEmail != null && !siteEmail.trim().isEmpty()) {
                String adminSubject = "New Contact Message: " + contactMessage.getSubject();
                StringBuilder adminBody = new StringBuilder();
                adminBody.append("You have received a new contact message:\n\n");
                adminBody.append("Name: ").append(contactMessage.getName()).append("\n");
                adminBody.append("Email: ").append(contactMessage.getEmail()).append("\n");
                adminBody.append("Subject: ").append(contactMessage.getSubject()).append("\n\n");
                adminBody.append("Message:\n").append(contactMessage.getMessage()).append("\n\n");
                adminBody.append("---\n");
                adminBody.append("Sent from AgriGrow contact form");

                logger.info("Sending notification to ADMIN: {}", siteEmail);
                adminEmailSent = emailService.trySendSimpleMail(siteEmail, adminSubject, adminBody.toString());
                logger.info("Admin notification email result: {}", adminEmailSent ? "SUCCESS" : "FAILED");
            } else {
                logger.warn("Site email not configured - skipping admin notification");
            }

            // 2. Send auto-reply acknowledgment to USER
            if (contactMessage.getEmail() != null && !contactMessage.getEmail().trim().isEmpty()) {
                String userSubject = "We Received Your Message - AgriGrow";
                String userBody = "Dear " + contactMessage.getName() + ",\n\n" +
                        "Thank you for contacting AgriGrow! We have successfully received your message and our team will review it shortly.\n\n" +
                        "Here's a summary of your message:\n" +
                        "Subject: " + contactMessage.getSubject() + "\n" +
                        "Message: " + (contactMessage.getMessage().length() > 100 ? 
                                      contactMessage.getMessage().substring(0, 100) + "..." : 
                                      contactMessage.getMessage()) + "\n\n" +
                        "We typically respond within 24 hours. If your matter is urgent, please feel free to call us at +1 234 567 890.\n\n" +
                        "Best regards,\n" +
                        "AgriGrow Team\n" +
                        "jyotsnatalasila@gmail.com";

                logger.info("Sending auto-reply to USER: {}", contactMessage.getEmail());
                userAckSent = emailService.trySendSimpleMail(contactMessage.getEmail(), userSubject, userBody);
                logger.info("User auto-reply email result: {}", userAckSent ? "SUCCESS" : "FAILED");
            }

        } catch (Exception e) {
            logger.error("Unexpected error during email processing: {}", e.getMessage(), e);
        }

        result.put("adminEmailSent", adminEmailSent);
        result.put("userAckSent", userAckSent);
        logger.info("=== CONTACT FORM PROCESSING COMPLETE ===");
        
        return result;
    }
}
