package org.example.appsmallcrm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.appsmallcrm.dto.*;
import org.example.appsmallcrm.entity.User;
import org.example.appsmallcrm.entity.embeddable.Role;
import org.example.appsmallcrm.exception.BadRequestException;
import org.example.appsmallcrm.exception.ResourceNotFoundException;
import org.example.appsmallcrm.mapper.UserMapper;
import org.example.appsmallcrm.repo.UserRepository;
import org.example.appsmallcrm.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional // Make service methods transactional by default
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ActivityService activityService;

    public UserDTO createUser(UserCreateDTO createDTO) {
        if (userRepository.existsByUsername(createDTO.getUsername())) {
            throw new BadRequestException("Username '" + createDTO.getUsername() + "' already exists.");
        }
        if (createDTO.getEmail() != null && userRepository.existsByEmail(createDTO.getEmail())) {
            throw new BadRequestException("Email '" + createDTO.getEmail() + "' already registered.");
        }

        User user = userMapper.fromCreateDTO(createDTO);
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setActive(true); // New users are active by default
        user.setCreatedAt(LocalDateTime.now());
        // Role is set from DTO, defaults to ROLE_USER if not provided by UserCreateDTO logic

        User savedUser = userRepository.save(user);
        log.info("User created: {}", savedUser.getUsername());
        activityService.recordActivity(getCurrentUsernameOrSystem(), "USER_CREATED", "User " + savedUser.getUsername() + " created with role " + savedUser.getRole());
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        // Add specification for filtering if needed
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    public UserDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check for username conflict if username is being changed
        if (updateDTO.getUsername() != null && !updateDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateDTO.getUsername())) {
                throw new BadRequestException("Username '" + updateDTO.getUsername() + "' already taken.");
            }
        }
        // Check for email conflict if email is being changed
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new BadRequestException("Email '" + updateDTO.getEmail() + "' already registered by another user.");
            }
        }

        userMapper.updateUserFromDto(updateDTO, user);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getUsername());
        activityService.recordActivity(getCurrentUsernameOrSystem(), "USER_UPDATED", "User " + updatedUser.getUsername() + " (ID: " + id + ") updated.");
        return userMapper.toDto(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Prevent self-deletion or deleting the last admin (add more sophisticated logic if needed)
        User currentUser = getCurrentAuthenticatedUserEntity();
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new BadRequestException("You cannot delete your own account.");
        }
        if (user.getRole() == Role.ROLE_ADMIN && countAdminUsers() <= 1) {
            throw new BadRequestException("Cannot delete the last administrator account.");
        }

        userRepository.delete(user);
        log.info("User deleted: {}", user.getUsername());
        activityService.recordActivity(getCurrentUsernameOrSystem(), "USER_DELETED", "User " + user.getUsername() + " (ID: " + id + ") deleted.");
    }

    private long countAdminUsers() {
        return userRepository.findAll().stream().filter(u -> u.getRole() == Role.ROLE_ADMIN).count();
    }

    public void changePassword(Long userId, ChangePasswordRequestDTO changePasswordDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        User currentUser = getCurrentAuthenticatedUserEntity();
        boolean isAdminChangingForOtherUser = currentUser != null && currentUser.getRole() == Role.ROLE_ADMIN && !currentUser.getId().equals(userId);

        // If it's not an admin changing someone else's password, then oldPassword is required and must match
        if (!isAdminChangingForOtherUser) {
            if (changePasswordDTO.getOldPassword() == null || changePasswordDTO.getOldPassword().isBlank()) {
                throw new BadRequestException("Old password is required when changing your own password.");
            }
            if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
                throw new BadRequestException("Incorrect old password.");
            }
        }
        // For admin changing password, changePasswordDTO.getOldPassword() might be null, which is fine.

        // Validate new password strength
        if (changePasswordDTO.getNewPassword() == null || changePasswordDTO.getNewPassword().length() < 8) {
            throw new BadRequestException("New password must be at least 8 characters long.");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
        activityService.recordActivity(getCurrentUsernameOrSystem(), "PASSWORD_CHANGED", "Password changed for user " + user.getUsername());
    }


    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        User user = getCurrentAuthenticatedUserEntity();
        if (user == null) {
            throw new ResourceNotFoundException("Authenticated user not found in security context or database.");
        }
        return userMapper.toDto(user);
    }


    // Helper to get current User entity
    public User getCurrentAuthenticatedUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).user();
        } else if (principal instanceof String username) { // If only username is stored
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    public String getCurrentUsernameOrSystem() {
        User user = getCurrentAuthenticatedUserEntity();
        return (user != null) ? user.getUsername() : "SYSTEM";
    }
}