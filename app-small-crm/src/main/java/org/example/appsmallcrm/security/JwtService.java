package org.example.appsmallcrm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.example.appsmallcrm.entity.User; // Assuming User entity is used for generation
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    private final SecretKey accessSigningKey;
    private final long accessExpirationMs;
    private final SecretKey refreshSigningKey;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${app.jwt.access.secretKey}") String accessSecret,
            @Value("${app.jwt.access.expirationAt}") long accessExpirationMs,
            @Value("${app.jwt.refresh.secretKey}") String refreshSecret,
            @Value("${app.jwt.refresh.expirationAt}") long refreshExpirationMs) {
        this.accessSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        this.refreshExpirationMs = refreshExpirationMs;
    }

    // --- Token Generation ---

    public String generateAccessToken(User user) {
        // You can include additional claims if needed
        Map<String, Object> extraClaims = new HashMap<>();
        // extraClaims.put("userId", user.getId()); // Example: if you also want user ID
        // extraClaims.put("role", user.getRole().name()); // Example: if you want role directly in token (careful with staleness)
        return buildToken(extraClaims, user.getUsername(), accessExpirationMs, accessSigningKey);
    }

    public String generateRefreshToken(User user) {
        // Refresh tokens typically have fewer claims, just the subject (username or ID)
        return buildToken(new HashMap<>(), user.getUsername(), refreshExpirationMs, refreshSigningKey);
        // Or, if refresh token uses ID as subject:
        // return buildToken(new HashMap<>(), user.getId().toString(), refreshExpirationMs, refreshSigningKey);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs, SecretKey signingKey) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    // --- Token Parsing and Validation ---

    private Claims extractAllClaims(String token, SecretKey key) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e; // Re-throw to be caught by JwtFilter or calling service
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty or invalid: {}", e.getMessage());
            throw e;
        }
    }

    public <T> T extractClaim(String token, SecretKey key, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    public String extractUsernameFromAccessToken(String token) {
        return extractClaim(token, accessSigningKey, Claims::getSubject);
    }

    public String extractUsernameFromRefreshToken(String token) {
        // Assuming refresh token also uses username as subject. If it uses ID, change this.
        return extractClaim(token, refreshSigningKey, Claims::getSubject);
    }

    // If refresh token subject is User ID:
    // public String extractUserIdFromRefreshToken(String token) {
    //     return extractClaim(token, refreshSigningKey, Claims::getSubject);
    // }


    private Date extractExpiration(String token, SecretKey key) {
        return extractClaim(token, key, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token, SecretKey key) {
        try {
            return extractExpiration(token, key).before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // Explicitly handle if extractExpiration itself throws ExpiredJwtException
        } catch (Exception e) {
            log.debug("Failed to check token expiration, considering it expired: {}", e.getMessage());
            return true; // Any other parsing error implies invalid/expired
        }
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsernameFromAccessToken(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, accessSigningKey);
        } catch (Exception e) { // Catch parsing exceptions from extractUsername or isTokenExpired
            log.debug("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        // Adapt if refresh token uses User ID as subject
        try {
            final String username = extractUsernameFromRefreshToken(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, refreshSigningKey);
        } catch (Exception e) {
            log.debug("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Example if Refresh Token's subject is User ID
    // public boolean isRefreshTokenValidById(String token, User userEntity) {
    //     try {
    //         final String userIdFromToken = extractUserIdFromRefreshToken(token);
    //         return (userIdFromToken.equals(userEntity.getId().toString())) && !isTokenExpired(token, refreshSigningKey);
    //     } catch (Exception e) {
    //         log.debug("Refresh token validation by ID failed: {}", e.getMessage());
    //         return false;
    //     }
    // }
}