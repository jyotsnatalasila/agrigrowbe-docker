package com.example.agrigrowbe.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.agrigrowbe.model.ContactMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
        try {
            // Try to read configured username for informational logging
            // This will help diagnose misconfigured SMTP credentials in containers
            String username = System.getenv("SPRING_MAIL_USERNAME");
            if (username != null) {
                System.out.println("INFO: EmailService configured with username: " + username);
            }
        } catch (Exception ignored) {}
    }

    public void sendSimpleMail(String to, String subject, String text){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            logger.info("📧 Preparing to send email - From: {}, To: {}, Subject: {}", fromEmail, to, subject);
            mailSender.send(message);
            logger.info("✅ Email sent successfully to: {}", to);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send email to: {}", to, e);
            throw e; // Re-throw to be caught by trySendSimpleMail
        }
    }

    public boolean trySendSimpleMail(String to, String subject, String text) {
        try {
            logger.info("🔄 Attempting to send email from: {} to: {}", fromEmail, to);
            logger.info("📝 Email subject: {}", subject);
            
            sendSimpleMail(to, subject, text);
            
            logger.info("🎉 Email sent successfully to: {}", to);
            return true;
        } catch (Exception ex) {
            logger.error("💥 Failed to send email to: {}", to);
            logger.error("🔴 Error details: {}", ex.getMessage());
            if (ex.getCause() != null) {
                logger.error("🔴 Root cause: {}", ex.getCause().getMessage());
            }
            return false;
        }
    }
     public boolean sendAdminNotification(ContactMessage message) {
        String subject = "New Contact Message from " + message.getName();
        String text = "You received a new message:\n\n"
                      + "Name: " + message.getName() + "\n"
                      + "Email: " + message.getEmail() + "\n"
                      + "Subject: " + message.getSubject() + "\n"
                      + "Message: " + message.getMessage() + "\n";
        return trySendSimpleMail(fromEmail, subject, text);
    }

    public boolean sendUserAcknowledgment(String toEmail, String userName) {
        String subject = "Thank you for contacting AgriGrow";
        String text = "Dear " + userName + ",\n\n"
                      + "Thank you for your message. Our technical team will get back to you shortly within 24 hours. Keep Shopping with us to avail huge discounts🎉.\n\n"
                      + "Best Regards,\nAgriGrow Team";
        return trySendSimpleMail(toEmail, subject, text);
    }
}