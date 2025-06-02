package org.example.appsmallcrm.entity.embeddable;

public enum OrderStatus {
    PENDING,       // Order placed, awaiting processing
    PROCESSING,    // Order is being prepared
    SHIPPED,       // Order has been shipped
    DELIVERED,     // Order has been delivered
    CANCELLED,     // Order was cancelled
    RETURNED       // Order was returned
}