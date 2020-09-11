package com.kds.tone.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimiterServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterServiceTest.class);
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
        boolean [] actual = invokeIsAllowed("1234", 3);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void isAllowed_should_return_false_when_same_users_requests_hits_gt_max_times_within_time_window() {
        boolean [] expected = {true, true, true, false};
        boolean [] actual = invokeIsAllowed("1234", 4);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void isAllowed_should_return_true_when_two_user_hits_1st_time() {
        String userIdFirstUser = "1234";
        boolean allowedFirstUser = rateLimiterService.isAllowed(userIdFirstUser);
        assertTrue(allowedFirstUser);

        String userIdSecondUser = "5678";
        boolean allowedSecondUser = rateLimiterService.isAllowed(userIdSecondUser);
        assertTrue(allowedSecondUser);
    }

    @Test
    public void isAllowed_should_not_allow_one_request_when_one_user_hits_concurrently_gt_max_requests() throws BrokenBarrierException, InterruptedException {
        final String userIdFirstUser = "1234";
        CyclicBarrier barrier = new CyclicBarrier(5);
        CountDownLatch latch = new CountDownLatch(4);
        ConcurrentLinkedQueue<Boolean> resultsQueue = new ConcurrentLinkedQueue<>();

        // create 4 threads which will start and invoke the isAllowed at the (near) same time.
        Set<Thread> userRequests = new HashSet<>();
        for (int i = 1; i < 5; i++) {
            String threadName = "u1Thread" + i;
            Thread u1Thread1 = new Thread(() -> {
                try {
                    barrier.await();
                    boolean allowed = rateLimiterService.isAllowed(userIdFirstUser);
                    resultsQueue.add(allowed);
                    latch.countDown();
                } catch (InterruptedException e) {
                    LOGGER.error("Error while awaiting thread", e);
                    Thread.currentThread().interrupt();
                } catch (BrokenBarrierException e) {
                    LOGGER.error("Barrier broken", e);
                }
            }, threadName);

            userRequests.add(u1Thread1);
        }

        // start all threads
        userRequests.forEach(Thread::start);
        // use the test worked to open the barrier
        barrier.await();
        // make the test worker wait until all request threads complete their work.
        latch.await();

        List<Boolean> actual = new ArrayList<>(resultsQueue);
        List<Boolean> expected = Arrays.asList(true, true, true, false);
        assertThat("Should contain results in any order", actual, containsInAnyOrder(expected.toArray()));
    }

    private boolean [] invokeIsAllowed(String userId, int numberOfTimes) {
        boolean [] results = new boolean [numberOfTimes];
        for (int i = 0; i < numberOfTimes; i++) {
            results[i] = rateLimiterService.isAllowed(userId);
        }
        return results;
    }
}
