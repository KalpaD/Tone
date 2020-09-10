package com.kds.tone.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    public void setUp() {
        rateLimiterService = new RateLimiterService(3, 4);
    }

    @Test
    public void isAllowed_should_return_true_when_new_user_hits_1st_time() {
        String userId = "1234";
        boolean allowed = rateLimiterService.isAllowed(userId);
        assertTrue(allowed);
    }

    @Test
    public void isAllowed_should_return_true_when_same_users_hits_2nd_time_after_time_window() throws InterruptedException {
        String userId = "1234";
        boolean allowedFirstTime = rateLimiterService.isAllowed(userId);
        assertTrue(allowedFirstTime);

        // wait for 5 seconds, and hit again
        Thread.sleep(5000);
        boolean allowedAfter3Secs = rateLimiterService.isAllowed(userId);
        assertTrue(allowedAfter3Secs);
    }

    @Test
    public void isAllowed_should_return_true_when_same_users_requests_hits_max_times_within_time_window() {
        boolean [] expected = {true, true, true};
        boolean[] actual = invokeIsAllowed("1234", 3);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void isAllowed_should_return_false_when_same_users_requests_hits_gt_max_times_within_time_window() {
        boolean [] expected = {true, true, true, false};
        boolean[] actual = invokeIsAllowed("1234", 4);
        assertArrayEquals(expected, actual);
    }

    private boolean [] invokeIsAllowed(String userId, int numberOfTimes) {
        boolean [] results = new boolean [numberOfTimes];
        for (int i = 0; i < numberOfTimes; i++) {
            results[i] = rateLimiterService.isAllowed(userId);
        }
        return results;
    }
}
