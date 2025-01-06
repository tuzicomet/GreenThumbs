package nz.ac.canterbury.seng302.gardenersgrove.utility;

import nz.ac.canterbury.seng302.gardenersgrove.exceptions.RateLimitException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aspect for rate limiting annotated methods.
 * This aspect intercepts methods annotated with {@link RateLimiter} and ensures that
 * the number of calls from a specific IP address does not exceed the configured rate limit.
 */
@Aspect
@Component
public class RateLimitAspect {

    private final ConcurrentHashMap<String, List<Long>> requestCounts = new ConcurrentHashMap<>();
    private int rateLimit = 2;
    private long rateDuration = 1000;
    private static final Logger LOG = LoggerFactory.getLogger(RateLimitAspect.class);

    /**
     * Method to handle rate limiting logic.
     * This method is executed before any method annotated with {@link RateLimiter}.
     * It tracks the number of requests from each IP address and throws a {@link RateLimitException}
     * if the rate limit is exceeded.
     *
     * @throws RateLimitException if the rate limit is exceeded
     */
    @Before("@annotation(nz.ac.canterbury.seng302.gardenersgrove.utility.RateLimiter)")
    public void rateLimit() throws RateLimitException {
        LOG.info("rateLimit() called");
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String key = requestAttributes.getRequest().getRemoteAddr();
        long currentTime = System.currentTimeMillis();
        if (requestCounts.containsKey(key)) {
            Long lastRequestTime = requestCounts.get(key).getLast();
            if (currentTime - lastRequestTime <= rateDuration) {
                throw new RateLimitException("Request too frequent. Please wait a second");
            }
        }
        requestCounts.putIfAbsent(key, new ArrayList<>());
        requestCounts.get(key).add(currentTime);
        if (requestCounts.get(key).size() > rateLimit) {
            throw new RateLimitException("Request too frequent. Please wait a second");
        }
        cleanUpRequestCounts(currentTime);
    }

    /**
     * Cleans up the request counts map by removing old timestamps that are outside
     * the rate duration window.
     *
     * @param currentTime the current system time in milliseconds
     */
    private void cleanUpRequestCounts(final long currentTime) {
        requestCounts.values().forEach(l -> l.removeIf(t -> timeIsTooOld(currentTime, t)));
    }

    /**
     * Checks if a given timestamp is older than the allowed rate duration.
     *
     * @param currentTime the current system time in milliseconds
     * @param timeToCheck the timestamp to check
     * @return {@code true} if the timestamp is too old, {@code false} otherwise
     */
    private boolean timeIsTooOld(final long currentTime, final long timeToCheck) {
        return currentTime - timeToCheck > rateDuration;
    }
}
