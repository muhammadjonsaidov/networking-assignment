package org.example.appsmallcrm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor; // Added for convenience

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor // Added for convenience
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String actor; // Username or SYSTEM
    private String action; // e.g., "USER_LOGIN", "PRODUCT_CREATED"
    private String details; // e.g., "User john logged in", "Product 'Laptop' created"
    private LocalDateTime timestamp;

    public Activity(String actor, String action, String details) {
        this.actor = actor;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}