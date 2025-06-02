package org.example.appsmallcrm.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.appsmallcrm.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
            new ApiResponse<>(false, ex.getMessage(), null)
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // Log ex.getRootCause() or ex.getMostSpecificCause() for details
        log.error("Data integrity violation: {}", ex.getMessage());
        String message = "A database constraint was violated. This could be due to a duplicate entry or invalid foreign key.";
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cve) {
            // Try to get a more specific message if possible, e.g., from constraint name
            // This is DB-specific and can be complex.
            message = "Database constraint violation: " + cve.getConstraintName() + ". " + cve.getSQLException().getMessage();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(message));
    }
}
