package nz.ac.canterbury.seng302.gardenersgrove.utility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to apply rate limiting on methods.
 * This annotation can be used to mark methods that should be subjected to rate limiting,
 * to prevent them from being called more frequently than allowed.
 *
 * The rate limiting logic should be implemented separately, typically in an aspect or an interceptor
 * that checks for this annotation and enforces the rate limit.
 *
 * Usage example:
 * <pre>
 * {@code
 * @RateLimiter
 * public void someMethod() {
 *     // method implementation
 * }
 * }
 * </pre>
 *
 * <p>
 * This annotation has a retention policy of RUNTIME, meaning it is available at runtime for reflection.
 * It targets methods, so it can only be applied to method declarations.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimiter {
}
