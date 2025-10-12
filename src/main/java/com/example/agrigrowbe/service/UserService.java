package com.example.agrigrowbe.service;

import com.example.agrigrowbe.model.User;
import com.example.agrigrowbe.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User registerUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    // ADD THIS METHOD
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void sendThankYouEmail(String email, String name) {
        String subject = "Thank you for choosing us";
        String text = "Hello Dear " + name + ",\n\nThank you for choosing Agrigrow. Hope you will have a great experience while shopping with us.\n\nThank and best regards,\nAgrigrow Team";
        emailService.sendSimpleMail(email, subject, text);
    }

    public void updateResetToken(String token, String email){
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(u -> {
            u.setResetToken(token);
            userRepository.save(u);
        });
    }

    public Optional<User> getByResetToken(String token){
        return userRepository.findByResetToken(token);
    }

    public void updatePassword(User user, String newPassword){
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }

    public void sendPasswordResetEmail(String email, String resetLink){
        String subject = "Password Reset Request";
        String text = "Dear User,\n\nYou requested to reset your password. Click the link to reset your password:\n"
                + resetLink + "\n\nIf you did not request this, please ignore this email.\n\nRegards,\nAgriGrow Team";
        emailService.sendSimpleMail(email, subject, text);
    }

    public void sendPasswordResetSuccessEmail(String email){
        String subject = "Password Reset Successful";
        String text = "Dear User,\n\nYour password has been successfully reset.\n\nRegards,\nAgriGrow Team";
        emailService.sendSimpleMail(email,subject, text);
    }
}