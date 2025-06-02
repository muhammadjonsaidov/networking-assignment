package org.example.appsmallcrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.appsmallcrm.dto.*;
import org.example.appsmallcrm.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Ensure Role enum is ROLE_ADMIN, ROLE_USER
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        UserDTO newUser = userService.createUser(createDTO);
        return ResponseEntity.status(201).body(ApiResponse.success(newUser, "User created successfully by admin."));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(@PageableDefault(size = 10, sort = "username") Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and #id == @userService.getCurrentAuthenticatedUserEntity().getId())")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')") // Or more specific if needed
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO updateDTO) {
        UserDTO updatedUser = userService.updateUser(id, updateDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully."));
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(@Valid @RequestBody ChangePasswordRequestDTO passwordRequest) { // Corrected here
        Long currentUserId = userService.getCurrentAuthenticatedUserEntity().getId();
        // The ChangePasswordRequestDTO now correctly has oldPassword and newPassword
        userService.changePassword(currentUserId, passwordRequest);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully."));
    }

    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminChangeUserPassword(@PathVariable Long id, @Valid @RequestBody AdminChangePasswordDTO passwordRequest) { // Corrected here (assuming AdminChangePasswordDTO)
        // If AdminChangePasswordDTO only has newPassword:
        // Create a ChangePasswordRequestDTO instance for the service if it expects oldPassword (though it's null for admin)
        ChangePasswordRequestDTO changeDtoForService = new ChangePasswordRequestDTO();
        changeDtoForService.setNewPassword(passwordRequest.getNewPassword());
        // oldPassword will be null, service method needs to handle this for admin case

        userService.changePassword(id, changeDtoForService);
        return ResponseEntity.ok(ApiResponse.success(null, "User password changed by admin."));
    }
}