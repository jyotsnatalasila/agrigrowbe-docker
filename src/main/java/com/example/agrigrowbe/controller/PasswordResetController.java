package com.example.agrigrowbe.controller;

import com.example.agrigrowbe.model.User;
import com.example.agrigrowbe.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/password")
@CrossOrigin(origins = {"http://localhost:4000","http://localhost:8080"})
public class PasswordResetController {

    private final UserService userService;

    @Value("${app.frontend.url}") // ✅ ADD THIS
    private String frontendUrl;

    public PasswordResetController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request){
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email not found"));
        }
        
        String token = UUID.randomUUID().toString();
        userService.updateResetToken(token, email);
        
        // ✅ UPDATED - Use configured frontend URL
        String resetLink = frontendUrl + "/resetpassword?token=" + token;
        
        userService.sendPasswordResetEmail(email, resetLink);
        
        return ResponseEntity.ok(Map.of("message", "Password reset link sent successfully"));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token, 
                                          @RequestBody Map<String, String> request){
        String newPassword = request.get("password");
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }
        
        Optional<User> userOpt = userService.getByResetToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
        }
        
        User user = userOpt.get();
        userService.updatePassword(user, newPassword);
        userService.sendPasswordResetSuccessEmail(user.getEmail());
        
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}