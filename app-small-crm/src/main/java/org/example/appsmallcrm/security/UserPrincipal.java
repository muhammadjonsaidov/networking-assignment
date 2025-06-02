package org.example.appsmallcrm.security;

import org.example.appsmallcrm.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UserPrincipal(User user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Use the Role enum's name, e.g., "ROLE_ADMIN", "ROLE_USER"
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Add logic if account expiration is implemented
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Add logic for account locking
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Add logic for credential expiration
    }

    @Override
    public boolean isEnabled() {
        return user.isActive(); // Use the isActive flag from User entity
    }
}