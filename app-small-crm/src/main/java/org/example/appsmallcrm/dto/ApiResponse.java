package org.example.appsmallcrm.dto;

// No changes from before, but ensure it's present and used.
// Generic structure:
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    // Static factory methods can be useful
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data);
    }
    public static ApiResponse<?> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}