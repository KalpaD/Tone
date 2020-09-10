package com.kds.tone.service;

import com.kds.tone.model.RequestCounter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private int maxRequests;
    private int timeWindowInSeconds;
    private Map<String,RequestCounter> userToRequestCounterMap = new ConcurrentHashMap<>();

    public RateLimiterService(int maxRequests, int timeWindowInSeconds) {
        this.maxRequests = maxRequests;
        this.timeWindowInSeconds = timeWindowInSeconds;
    }

    public boolean isAllowed(String userId) {

        RequestCounter requestCounter = userToRequestCounterMap.get(userId);

        if (Objects.isNull(requestCounter)) {
            resetRequestCounter(userId);
            return true;
        }
        else {
            return handleExistingRequestCounter(userId, requestCounter);
        }
    }

    private void resetRequestCounter(String userId) {
        RequestCounter newRequestCounter = new RequestCounter(1, System.currentTimeMillis());
        userToRequestCounterMap.put(userId, newRequestCounter);
    }

    private boolean handleExistingRequestCounter(String userId, RequestCounter requestCounter) {
        long numberSecondsSinceFirstRequest = calculateElapsedSecondsSinceFirstRequest(requestCounter);
        if (numberSecondsSinceFirstRequest >= timeWindowInSeconds) {
            // need to reset the counter for this user
            userToRequestCounterMap.remove(userId);
            resetRequestCounter(userId);
            return true;
        }
        else {
            int currentNumOfRequests = requestCounter.getCounter();
            if (currentNumOfRequests < maxRequests) {
                incrementAndSetCounter(currentNumOfRequests, requestCounter);
                return true;
            }
            else {
                return false;
            }
        }
    }

    private long calculateElapsedSecondsSinceFirstRequest(RequestCounter requestCounter) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - requestCounter.getTimestamp()) / 1000;
    }

    private void incrementAndSetCounter(int currentNumOfRequests, RequestCounter requestCounter) {
        currentNumOfRequests++;
        requestCounter.setCounter(currentNumOfRequests);
    }
}
