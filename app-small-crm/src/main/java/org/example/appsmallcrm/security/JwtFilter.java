package org.example.appsmallcrm.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.appsmallcrm.service.CustomUserDetailsService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            // No token or not a Bearer token, proceed to next filter.
            // If the endpoint is protected, Spring Security will deny access later.
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7); // "Bearer ".length()
        String username = null;

        try {
            username = jwtService.extractUsernameFromAccessToken(jwt);
        } catch (ExpiredJwtException e) {
            log.warn("JWT Access Token has expired for path: {}. Message: {}", request.getRequestURI(), e.getMessage());
            // Optionally, you can set a response header or attribute here to indicate token expiry for client handling
            // response.setHeader("X-Token-Expired", "true");
            // For stateless, usually let Spring Security handle it, which will result in 401/403 if auth is required.
        } catch (JwtException e) { // Catches other JWT parsing/validation issues from JwtService
            log.warn("Invalid JWT Access Token for path: {}. Message: {}", request.getRequestURI(), e.getMessage());
        } catch (Exception e) { // Catch-all for unexpected errors during username extraction
            log.error("Unexpected error while extracting username from JWT: {}", e.getMessage(), e);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isAccessTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials are not needed as JWT is used
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.trace("Successfully authenticated user '{}' and set SecurityContext", username);
                } else {
                    log.warn("JWT token is invalid or expired for user '{}'", username);
                    // Token might be structurally valid but failed semantic validation (e.g. expired, wrong user)
                }
            } catch (UsernameNotFoundException e) {
                log.warn("User '{}' not found from JWT token.", username);
                // This case means token was for a user that no longer exists or is inactive.
            } catch (Exception e) {
                log.error("Error during user details loading or token validation for user '{}': {}", username, e.getMessage(), e);
            }
        } else if (username == null && StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            // This means token parsing failed (expired, malformed etc.) and username couldn't be extracted
            // Logging for this case is already done in the catch blocks above.
            // SecurityContext remains unauthenticated.
        }


        filterChain.doFilter(request, response);
    }
}