package org.example.appsmallcrm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.appsmallcrm.entity.embeddable.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private ProductDTO product; // Nested DTO for product details
    private CustomerDTO customer; // Nested DTO for customer details
    private UserDTO createdBy; // User who created the order
    private Integer quantity;
    private Double unitPrice;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDate orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}