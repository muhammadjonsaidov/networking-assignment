package org.example.appsmallcrm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.appsmallcrm.entity.embeddable.Role;

@Data
public class UserUpdateDTO {
    @Size(min = 3, max = 50)
    private String username; // Optional: if username change is allowed

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    private Role role;
    private Boolean isActive; // Use Boolean to differentiate between not provided and setting to false
}