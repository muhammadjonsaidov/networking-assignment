package org.example.appsmallcrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.appsmallcrm.entity.embeddable.Role;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Table(name = "users") // Ensure table name is explicitly set
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Size(min = 6) // Password length validation will be primarily in DTO
    @Column(nullable = false)
    private String password;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(max = 100)
    @Column(unique = true)
    private String email; // Can be nullable or unique based on requirements

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // Changed from String to Enum

    @Builder.Default
    private boolean isActive = true; // Default to active

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "createdBy") // If tracking who created an order
    private Set<Order> ordersCreated;

    // Helper for UserDetails
    public String getActualRole() {
        return role.name();
    }
}