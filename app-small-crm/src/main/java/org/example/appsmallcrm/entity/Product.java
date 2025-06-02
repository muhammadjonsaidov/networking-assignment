package org.example.appsmallcrm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @DecimalMin(value = "0.0", inclusive = false)
    private Double price;

    @Min(value = 0)
    private Integer stock;

    @Size(max = 50)
    private String status; // e.g., "Available", "OutOfStock", "Discontinued"

    @Size(max = 100)
    private String category; // New field

    @Size(max = 1000)
    private String description; // Optional: for more product details
}