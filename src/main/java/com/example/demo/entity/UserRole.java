package com.example.demo.entity;

public enum UserRole {
    ADMIN("관리자"),
    USER("일반사용자");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}