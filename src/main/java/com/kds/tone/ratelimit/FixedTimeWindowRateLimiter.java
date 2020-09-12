package com.kds.tone.ratelimit;

import com.kds.tone.model.RateLimitResults;
import com.kds.tone.model.RequestCounter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class provides the implementation of the fixed time window based rate limiting functionality.
 * The max number of requests and the tine window can be configured at the initiation time of the rate limiter.
 */
@Component
public class FixedTimeWindowRateLimiter implements RateLimiter {

    private final int maxRequests;
    private final int timeWindowInSeconds;
    private Map<String, RequestCounter> userToRequestCounterMap = new ConcurrentHashMap<>();
    private Lock rateLimiterLock;

    public FixedTimeWindowRateLimiter(@Value("${fixed.window.rate.limit.max.requests:100}") int maxRequests,
                                      @Value("${fixed.window.rate.limit.time.window.in.seconds:3600}") int timeWindowInSeconds) {
        this.maxRequests = maxRequests;
        this.timeWindowInSeconds = timeWindowInSeconds;
        this.rateLimiterLock = new ReentrantLock();
    }

    public RateLimitResults isAllowed(final String userId) {
        rateLimiterLock.lock();
        RequestCounter requestCounter = userToRequestCounterMap.get(userId);
        if (Objects.isNull(requestCounter)) {
            resetRequestCounter(userId);
            return createResults(true, 0);
        }
        else {
            return handleExistingRequestCounter(userId, requestCounter);
        }
    }

    private void resetRequestCounter(final String userId) {
        RequestCounter newRequestCounter = new RequestCounter(1, System.currentTimeMillis());
        userToRequestCounterMap.put(userId, newRequestCounter);
    }

    private RateLimitResults createResults(final boolean allowed, final long numberSecondsSinceFirstRequest) {
        long numOfSecondsUntilWindowReset = timeWindowInSeconds - numberSecondsSinceFirstRequest;
        rateLimiterLock.unlock();
        return new RateLimitResults(allowed, numOfSecondsUntilWindowReset);
    }

    private RateLimitResults handleExistingRequestCounter(final String userId, final RequestCounter requestCounter) {
        long numberSecondsSinceFirstRequest = calculateElapsedSecondsSinceFirstRequest(requestCounter);
        if (numberSecondsSinceFirstRequest >= timeWindowInSeconds) {
            return handleRequestOutsideTheTimeWindow(userId, numberSecondsSinceFirstRequest);
        }
        else {
            return handleRequestInsideTheTimeWindow(requestCounter, numberSecondsSinceFirstRequest);
        }
    }

    private RateLimitResults handleRequestOutsideTheTimeWindow(final String userId, final long numberSecondsSinceFirstRequest) {
        // reset the counter for this user
        userToRequestCounterMap.remove(userId);
        resetRequestCounter(userId);
        return createResults(true, numberSecondsSinceFirstRequest);
    }

    private RateLimitResults handleRequestInsideTheTimeWindow(final RequestCounter requestCounter, final long numberSecondsSinceFirstRequest) {
        int currentNumOfRequests = requestCounter.getCounter();
        if (currentNumOfRequests < maxRequests) {
            incrementAndSetCounter(currentNumOfRequests, requestCounter);
            return createResults(true, numberSecondsSinceFirstRequest);
        }
        else {
            return createResults(false, numberSecondsSinceFirstRequest);
        }
    }

    private long calculateElapsedSecondsSinceFirstRequest(final RequestCounter requestCounter) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - requestCounter.getTimestamp()) / 1000;
    }

    private void incrementAndSetCounter(int currentNumOfRequests, final RequestCounter requestCounter) {
        currentNumOfRequests++;
        requestCounter.setCounter(currentNumOfRequests);
    }
}
