package za.co.gymhubsa.api.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response envelope returned by {@code GlobalExceptionHandler}
 * for every failure path (validation, not-found, auth, unexpected errors).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String message,
        int status,
        String path,
        Instant timestamp,
        List<String> errors
) {

    public static ErrorResponse of(String message, int status, String path) {
        return new ErrorResponse(false, message, status, path, Instant.now(), null);
    }

    public static ErrorResponse of(String message, int status, String path, List<String> errors) {
        return new ErrorResponse(false, message, status, path, Instant.now(), errors);
    }
}
