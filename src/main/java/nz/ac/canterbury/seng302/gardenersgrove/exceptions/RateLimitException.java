package nz.ac.canterbury.seng302.gardenersgrove.exceptions;

/**
 * Exception thrown when a rate limit is exceeded.
 * This exception can be used to indicate that an operation has been attempted
 * too frequently and the rate limit policy has been violated.
 */
public class RateLimitException extends Exception {

    /**
     * Constructs a new {@code RateLimitException} with {@code null} as its detail message.
     * The cause is not initialized.
     */
    public RateLimitException() {
        super();
    }

    /**
     * Constructs a new {@code RateLimitException} with the specified detail message.
     * The cause is not initialized.
     *
     * @param message the detail message
     */
    public RateLimitException(String message) {
        super(message);
    }
}
