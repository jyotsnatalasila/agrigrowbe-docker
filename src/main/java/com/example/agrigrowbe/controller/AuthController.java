package com.example.agrigrowbe.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.agrigrowbe.model.User;
import com.example.agrigrowbe.security.JwtUtils;
import com.example.agrigrowbe.service.UserService;
import com.example.agrigrowbe.service.EmailService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Changed to allow all origins
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final EmailService emailService;
    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService, EmailService emailService){
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.emailService = emailService;
    }
    // Add this public endpoint
    @GetMapping("/public-test")
    public ResponseEntity<?> publicTest() {
        System.out.println("=== PUBLIC TEST ENDPOINT HIT ===");
        return ResponseEntity.ok(Map.of(
            "message", "Auth public endpoint is working!",
            "status", "success",
            "timestamp", System.currentTimeMillis()
        ));
    }

    // Your existing methods below - keep them as they are
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    private boolean isBlockedDomain(String email) {
        Set<String> blockedDomains = new HashSet<>();
        blockedDomains.add("mailinator.com");
        blockedDomains.add("tempmail.com");
        blockedDomains.add("10minutemail.com");

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        return blockedDomains.contains(domain);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        System.out.println("=== REGISTRATION REQUEST RECEIVED ===");
        System.out.println("Email: " + user.getEmail());
        System.out.println("Username: " + user.getUsername());

        try {
            if (!isValidEmail(user.getEmail())) {
                System.out.println("ERROR: Invalid email format");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Please enter a valid email address"));
            }

            if (isBlockedDomain(user.getEmail())) {
                System.out.println("ERROR: Disposable email domain blocked");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Disposable email addresses are not allowed"));
            }

            if (userService.findByEmail(user.getEmail()).isPresent()) {
                System.out.println("ERROR: Email already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email already registered"));
            }

            if (user.getPassword() == null || user.getPassword().length() < 6) {
                System.out.println("ERROR: Password too short");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Password must be at least 6 characters"));
            }

            System.out.println("Attempting to register user...");
            if (user.getFullName() == null) user.setFullName("");
            if (user.getPhone() == null) user.setPhone("");
            if (user.getAddressLine1() == null) user.setAddressLine1("");
            if (user.getAddressLine2() == null) user.setAddressLine2("");
            if (user.getColony() == null) user.setColony("");
            if (user.getCity() == null) user.setCity("");
            if (user.getState() == null) user.setState("");
            if (user.getPostalCode() == null) user.setPostalCode("");
            if (user.getCountry() == null) user.setCountry("");

            userService.registerUser(user);
            System.out.println("User registered successfully!");

            try {
                userService.sendThankYouEmail(user.getEmail(), user.getUsername());
            } catch (Exception e) {
                System.err.println("Failed to send registration email: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (Exception e) {
            System.out.println("EXCEPTION during registration: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String,String> loginRequest){
        System.out.println("=== LOGIN REQUEST RECEIVED ===");
        System.out.println("Email: " + loginRequest.get("email"));

        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            if (email == null || password == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email and password are required"));
            }

            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );

            String jwt = jwtUtils.generateJwtToken(email);
            System.out.println("✅ Login successful - JWT generated for: " + email);

            // Increment login count for analytics
            userService.findByEmail(email).ifPresent(u -> {
                int c = u.getLoginCount();
                u.setLoginCount(c + 1);
                u.setLastLogin(java.time.Instant.now());
                userService.updateUser(u);
            });

            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e){
            System.out.println("❌ Bad credentials for: " + loginRequest.get("email"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        } catch (AuthenticationException e) {
            System.out.println("❌ Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed"));
        } catch (Exception e) {
            System.out.println("EXCEPTION during login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuthentication(Authentication authentication) {
        System.out.println("=== AUTHENTICATION TEST ===");
        
        if (authentication == null) {
            System.out.println("❌ Authentication is NULL");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No authentication found", "authenticated", false));
        }
        
        System.out.println("✅ Authentication found:");
        System.out.println("Name: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());
        System.out.println("Authenticated: " + authentication.isAuthenticated());
        
        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "username", authentication.getName(),
            "authorities", authentication.getAuthorities().toString()
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            System.out.println("📥 Fetching profile for: " + email);
            
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            System.out.println("👤 Found user - FullName: " + user.getFullName() + ", Phone: " + user.getPhone());
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("email", user.getEmail());
            profile.put("username", user.getUsername());
            profile.put("fullName", user.getFullName());
            profile.put("phone", user.getPhone());
            profile.put("addressLine1", user.getAddressLine1());
            profile.put("addressLine2", user.getAddressLine2());
            profile.put("colony", user.getColony());
            profile.put("city", user.getCity());
            profile.put("state", user.getState());
            profile.put("postalCode", user.getPostalCode());
            profile.put("country", user.getCountry());
            
            System.out.println("📤 Sending profile data: " + profile);
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            System.out.println("❌ Error fetching profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch profile: " + e.getMessage()));
        }
    }
    @PostMapping("/test-email")
public ResponseEntity<?> testEmail(@RequestBody Map<String, String> request) {
    try {
        String toEmail = request.get("email");
        if (toEmail == null || toEmail.trim().isEmpty()) {
            toEmail = "jyotsnatalasila@gmail.com";
        }
        
        System.out.println("🧪 Testing email configuration...");
        System.out.println("From: jyotsnatalasila@gmail.com");
        System.out.println("To: " + toEmail);
        
        boolean success = emailService.trySendSimpleMail(
            toEmail, 
            "Test Email from AgriGrow", 
            "This is a test email to verify SMTP configuration is working correctly."
        );
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "message", "Test email sent successfully!",
                "status", "SUCCESS"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Failed to send test email",
                        "status", "FAILED"
                    ));
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Test email failed: " + e.getMessage(),
                    "status", "ERROR"
                ));
    }
}
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            System.out.println("🔄 Updating profile for: " + email);
            System.out.println("📦 Received data: " + profileData);
            
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            System.out.println("👤 Current user data - FullName: " + user.getFullName() + ", Phone: " + user.getPhone());
            
            // Update user fields
            if (profileData.containsKey("fullName")) {
                user.setFullName(profileData.get("fullName"));
            }
            if (profileData.containsKey("phone")) {
                user.setPhone(profileData.get("phone"));
            }
            if (profileData.containsKey("addressLine1")) {
                user.setAddressLine1(profileData.get("addressLine1"));
            }
            if (profileData.containsKey("addressLine2")) {
                user.setAddressLine2(profileData.get("addressLine2"));
            }
            if (profileData.containsKey("colony")) {
                user.setColony(profileData.get("colony"));
            }
            if (profileData.containsKey("city")) {
                user.setCity(profileData.get("city"));
            }
            if (profileData.containsKey("state")) {
                user.setState(profileData.get("state"));
            }
            if (profileData.containsKey("postalCode")) {
                user.setPostalCode(profileData.get("postalCode"));
            }
            if (profileData.containsKey("country")) {
                user.setCountry(profileData.get("country"));
            }
            
            System.out.println("💾 Saving updated user...");
            User updatedUser = userService.updateUser(user);
            System.out.println("✅ User saved successfully");
            
            // Return the updated user data
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("email", updatedUser.getEmail());
            response.put("username", updatedUser.getUsername());
            response.put("fullName", updatedUser.getFullName());
            response.put("phone", updatedUser.getPhone());
            response.put("addressLine1", updatedUser.getAddressLine1());
            response.put("addressLine2", updatedUser.getAddressLine2());
            response.put("colony", updatedUser.getColony());
            response.put("city", updatedUser.getCity());
            response.put("state", updatedUser.getState());
            response.put("postalCode", updatedUser.getPostalCode());
            response.put("country", updatedUser.getCountry());
            response.put("message", "Profile updated successfully");
            
            System.out.println("📤 Sending response: " + response);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        }
    }
}