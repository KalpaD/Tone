package com.kds.tone.service;

import com.kds.tone.model.RequestCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RateLimiterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterService.class);
    private final int maxRequests;
    private final int timeWindowInSeconds;
    private Map<String, RequestCounter> userToRequestCounterMap = new ConcurrentHashMap<>();
    private Lock rateLimiterLock;

    public RateLimiterService(int maxRequests, int timeWindowInSeconds) {
        this.maxRequests = maxRequests;
        this.timeWindowInSeconds = timeWindowInSeconds;
        this.rateLimiterLock = new ReentrantLock();
    }

    public boolean isAllowed(final String userId) {
        rateLimiterLock.lock();
        RequestCounter requestCounter = userToRequestCounterMap.get(userId);
        if (Objects.isNull(requestCounter)) {
            resetRequestCounter(userId);
            return true;
        }
        else {
            return handleExistingRequestCounter(userId, requestCounter);
        }
    }

    private void resetRequestCounter(final String userId) {
        RequestCounter newRequestCounter = new RequestCounter(1, System.currentTimeMillis());
        userToRequestCounterMap.put(userId, newRequestCounter);
        rateLimiterLock.unlock();
    }

    private boolean handleExistingRequestCounter(final String userId, final RequestCounter requestCounter) {
        long numberSecondsSinceFirstRequest = calculateElapsedSecondsSinceFirstRequest(requestCounter);
        if (numberSecondsSinceFirstRequest >= timeWindowInSeconds) {
            // reset the counter for this user
            userToRequestCounterMap.remove(userId);
            resetRequestCounter(userId);
            return true;
        }
        else {
            int currentNumOfRequests = requestCounter.getCounter();
            if (currentNumOfRequests < maxRequests) {
                incrementAndSetCounter(currentNumOfRequests, requestCounter);
                rateLimiterLock.unlock();
                return true;
            }
            else {
                rateLimiterLock.unlock();
                return false;
            }
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
