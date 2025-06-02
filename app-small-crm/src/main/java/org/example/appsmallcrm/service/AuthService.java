package org.example.appsmallcrm.service;

import lombok.extern.slf4j.Slf4j;
import org.example.appsmallcrm.dto.LoginDTO;
import org.example.appsmallcrm.dto.RefreshTokenRequestDTO;
import org.example.appsmallcrm.dto.TokenDTO;
import org.example.appsmallcrm.entity.User;
import org.example.appsmallcrm.exception.InvalidTokenException;
import org.example.appsmallcrm.repo.UserRepository;
import org.example.appsmallcrm.security.JwtService;
import org.example.appsmallcrm.security.UserPrincipal; // Make sure UserPrincipal is used or adapt to UserDetails
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository; // Still needed to fetch User for token generation
    private final JwtService jwtService;
    private final ActivityService activityService;
    private final CustomUserDetailsService customUserDetailsService; // Added for loading UserDetails

    @Autowired
    public AuthService(@Lazy AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService,
                       ActivityService activityService,
                       CustomUserDetailsService customUserDetailsService) { // Injected CustomUserDetailsService
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.activityService = activityService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Transactional
    public TokenDTO login(LoginDTO loginDTO) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    ));
        } catch (BadCredentialsException e) {
            log.warn("Login attempt failed for user {}: Invalid credentials", loginDTO.getUsername());
            activityService.recordActivity(loginDTO.getUsername(), "LOGIN_FAILED", "Invalid credentials for user " + loginDTO.getUsername());
            throw e; // Re-throw to be caught by GlobalExceptionHandler
        } catch (DisabledException e) {
            log.warn("Login attempt failed for user {}: Account disabled", loginDTO.getUsername());
            activityService.recordActivity(loginDTO.getUsername(), "LOGIN_FAILED", "Account disabled for user " + loginDTO.getUsername());
            throw new InvalidTokenException("User account is inactive."); // Or a custom exception for disabled account
        }


        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.user(); // Get the User entity

        // isActive check is already handled by UserPrincipal.isEnabled() and authenticationManager
        // but an explicit check here is fine for clarity if UserPrincipal logic changes.
        if (!user.isActive()) {
            log.warn("Login attempt by inactive user: {}", user.getUsername());
            activityService.recordActivity(user.getUsername(), "LOGIN_FAILED_INACTIVE", "Attempted login by inactive user " + user.getUsername());
            throw new InvalidTokenException("User account is inactive.");
        }


        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        TokenDTO tokenDTO = TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        log.info("User logged in: {}", user.getUsername());
        activityService.recordActivity(user.getUsername(), "USER_LOGIN", "User " + user.getUsername() + " logged in successfully.");
        return tokenDTO;
    }

    @Transactional
    public TokenDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        String requestRefreshToken = refreshTokenRequestDTO.getRefreshToken();
        String username;

        try {
            // Assuming refresh token subject is username, consistent with JwtService changes
            username = jwtService.extractUsernameFromRefreshToken(requestRefreshToken);
        } catch (Exception e) {
            log.warn("Invalid refresh token structure or signature: {}", e.getMessage());
            activityService.recordActivity("UNKNOWN_USER", "INVALID_REFRESH_TOKEN_STRUCTURE", "Invalid refresh token structure presented.");
            throw new InvalidTokenException("Invalid refresh token.", e);
        }

        UserDetails userDetails;
        User userEntity; // We need the User entity to generate a new access token
        try {
            userDetails = customUserDetailsService.loadUserByUsername(username);
            // Assuming UserDetails is UserPrincipal, which contains the User entity
            if (userDetails instanceof UserPrincipal) {
                userEntity = ((UserPrincipal) userDetails).user();
            } else {
                // Fallback if UserDetails is not UserPrincipal - this shouldn't happen with current setup
                // but as a safeguard, refetch the user. This is less efficient.
                log.warn("UserDetails is not an instance of UserPrincipal for user {}. Refetching user.", username);
                userEntity = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            }
        } catch (UsernameNotFoundException e) {
            log.warn("Refresh token presented for non-existent or inactive user: {}", username);
            activityService.recordActivity(username, "REFRESH_TOKEN_USER_NOT_FOUND", "Refresh token for non-existent/inactive user " + username);
            throw new InvalidTokenException("User associated with refresh token not found or inactive.", e);
        }

        // Check if account is active (UserDetails.isEnabled() checks this via UserPrincipal)
        if (!userDetails.isEnabled()) {
            log.warn("Refresh token attempt by inactive user: {}", username);
            activityService.recordActivity(username, "REFRESH_TOKEN_INACTIVE_USER", "Refresh token attempt by inactive user " + username);
            throw new InvalidTokenException("User account is inactive.");
        }


        // Now validate the refresh token against the loaded UserDetails
        if (jwtService.isRefreshTokenValid(requestRefreshToken, userDetails)) {
            // Generate new access token using the User entity
            String newAccessToken = jwtService.generateAccessToken(userEntity);

            // Optionally, rotate refresh token
            // String newRefreshToken = jwtService.generateRefreshToken(userEntity);
            // activityService.recordActivity(username, "TOKEN_REFRESHED", "Token rotated.");

            TokenDTO tokenDTO = TokenDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(requestRefreshToken) // or newRefreshToken if rotating
                    .build();

            log.info("Token refreshed for user: {}", username);
            activityService.recordActivity(username, "TOKEN_REFRESHED", "Access token refreshed for user " + username);
            return tokenDTO;
        } else {
            log.warn("Invalid refresh token presented for user: {}", username);
            activityService.recordActivity(username, "INVALID_REFRESH_TOKEN_VALIDATION", "Invalid refresh token presented for user " + username);
            throw new InvalidTokenException("Invalid or expired refresh token.");
        }
    }
}