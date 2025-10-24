package com.example.agrigrowbe.controller;

import com.example.agrigrowbe.model.User;
import com.example.agrigrowbe.dto.AdminUserDto;
import com.example.agrigrowbe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://ec2-16-16-179-64.eu-north-1.compute.amazonaws.com:4000", "http://ec2-16-16-179-64.eu-north-1.compute.amazonaws.com:8000"})
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Value("${spring.security.user.password}")
    private String adminPassword;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<AdminUserDto> dtos = users.stream().map(user -> {
                AdminUserDto dto = new AdminUserDto();
                dto.setId(user.getId());
                dto.setName(user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : user.getUsername());
                dto.setEmail(user.getEmail());
                dto.setLoginCount(user.getLoginCount());
                
                boolean active = false;
                if (user.getLastLogin() != null) {
                    Instant lastLogin = user.getLastLogin();
                    active = Duration.between(lastLogin, Instant.now()).toMinutes() <= 30;
                }
                dto.setActive(active);
                
                return dto;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Alternate lightweight access: supply X-Admin-Password header matching application.properties
    @GetMapping("/users/unsecured")
    public ResponseEntity<?> getAllUsersWithPassword(@RequestHeader(value = "X-Admin-Password", required = false) String password) {
        try {
            if (password == null || !password.equals(adminPassword)) {
                return ResponseEntity.status(401).body("Unauthorized: Invalid admin password");
            }
            
            List<User> users = userRepository.findAll();
            List<AdminUserDto> dtos = users.stream().map(user -> {
                AdminUserDto dto = new AdminUserDto();
                dto.setId(user.getId());
                dto.setName(user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : user.getUsername());
                dto.setEmail(user.getEmail());
                dto.setLoginCount(user.getLoginCount());
                
                boolean active = false;
                if (user.getLastLogin() != null) {
                    Instant lastLogin = user.getLastLogin();
                    active = Duration.between(lastLogin, Instant.now()).toMinutes() <= 30;
                }
                dto.setActive(active);
                
                return dto;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving users: " + e.getMessage());
        }
    }
}
