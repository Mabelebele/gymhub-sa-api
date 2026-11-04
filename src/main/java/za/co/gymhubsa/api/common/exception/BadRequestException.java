package za.co.gymhubsa.api.common.exception;

/**
 * Thrown for invalid client input or state conflicts that are not covered by
 * bean validation. Mapped to HTTP 400 by {@link GlobalExceptionHandler}.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
