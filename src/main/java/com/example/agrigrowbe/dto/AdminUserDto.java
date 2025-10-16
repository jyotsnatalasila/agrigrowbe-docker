package com.example.agrigrowbe.dto;

import java.util.UUID;

public class AdminUserDto {
    private UUID id;
    private String name;
    private String email;
    private Integer loginCount;
    private boolean active;

    // Default constructor
    public AdminUserDto() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID uuid) {
        this.id = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}