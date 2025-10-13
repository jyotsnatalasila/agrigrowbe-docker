package com.example.agrigrowbe.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {
    @GetMapping("/")
    public Map<String, String> getRoot() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "AgriGrow Backend API is running");
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, String> getHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AgriGrow Backend");
        return response;
    }
}