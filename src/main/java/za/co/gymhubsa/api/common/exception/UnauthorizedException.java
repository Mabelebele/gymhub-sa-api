package za.co.gymhubsa.api.common.exception;

/**
 * Thrown for authentication failures (invalid credentials, expired/invalid
 * tokens). Mapped to HTTP 401 by {@link GlobalExceptionHandler}.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
