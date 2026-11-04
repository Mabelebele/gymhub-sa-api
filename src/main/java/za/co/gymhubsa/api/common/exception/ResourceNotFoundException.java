package za.co.gymhubsa.api.common.exception;

/**
 * Thrown when a requested entity (member, membership plan, trainer, etc.)
 * cannot be found. Mapped to HTTP 404 by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object identifier) {
        return new ResourceNotFoundException(resource + " not found with identifier: " + identifier);
    }
}
