package com.kds.tone.service;

import com.kds.tone.model.RateLimitResults;
import com.kds.tone.ratelimit.FixedTimeWindowRateLimiter;
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
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixedTimeWindowRateLimiterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixedTimeWindowRateLimiterTest.class);
    private FixedTimeWindowRateLimiter rateLimiter;

    @BeforeEach
    public void setUp() {
        rateLimiter = new FixedTimeWindowRateLimiter(3, 4);
    }

    @Test
    public void isAllowed_should_return_true_when_new_user_hits_1st_time() {
        String userId = "1234";
        RateLimitResults rateLimitResults = rateLimiter.isAllowed(userId);
        assertTrue(rateLimitResults.isAllowed());
    }

    @Test
    public void isAllowed_should_return_true_when_same_users_hits_2nd_time_after_time_window() throws InterruptedException {
        String userId = "1234";
        RateLimitResults rateLimitResultsFirstTime = rateLimiter.isAllowed(userId);
        assertTrue(rateLimitResultsFirstTime.isAllowed());

        // wait for 5 seconds, and hit again
        Thread.sleep(5000);
        RateLimitResults rateLimitResultsAfter3Secs = rateLimiter.isAllowed(userId);
        assertTrue(rateLimitResultsAfter3Secs.isAllowed());
    }

    @Test
    public void isAllowed_should_return_true_when_same_users_requests_hits_max_times_within_time_window() {
        List<Boolean> expected = Arrays.asList(true, true, true);
        List<Boolean> actual = invokeIsAllowed("1234", 3)
                .stream()
                .map(RateLimitResults::isAllowed).collect(Collectors.toList());
        assertThat(actual, is(expected));
    }

    @Test
    public void isAllowed_should_return_false_when_same_users_requests_hits_gt_max_times_within_time_window() {
        List<Boolean> expected = Arrays.asList(true, true, true, false);
        List<Boolean> actual = invokeIsAllowed("1234", 4)
                .stream()
                .map(RateLimitResults::isAllowed).collect(Collectors.toList());
        assertThat(actual, is(expected));
    }

    @Test
    public void isAllowed_should_return_true_when_two_user_hits_1st_time() {
        String userIdFirstUser = "1234";
        RateLimitResults rateLimitResultsFirstUser = rateLimiter.isAllowed(userIdFirstUser);
        assertTrue(rateLimitResultsFirstUser.isAllowed());

        String userIdSecondUser = "5678";
        RateLimitResults rateLimitResultsSecondUser = rateLimiter.isAllowed(userIdSecondUser);
        assertTrue(rateLimitResultsSecondUser.isAllowed());
    }

    @Test
    public void isAllowed_should_not_allow_one_request_when_one_user_hits_concurrently_gt_max_requests() throws BrokenBarrierException, InterruptedException {
        final String userIdFirstUser = "1234";
        CyclicBarrier barrier = new CyclicBarrier(5);
        CountDownLatch latch = new CountDownLatch(4);
        ConcurrentLinkedQueue<RateLimitResults> resultsQueue = new ConcurrentLinkedQueue<>();

        // create 4 threads which will start and invoke the isAllowed at the (near) same time.
        Set<Thread> userRequests = new HashSet<>();
        for (int i = 1; i < 5; i++) {
            String threadName = "u1Thread" + i;
            Thread u1Thread1 = new Thread(() -> {
                try {
                    barrier.await();
                    RateLimitResults rateLimitResults = rateLimiter.isAllowed(userIdFirstUser);
                    resultsQueue.add(rateLimitResults);
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
        // use the test worker to open the barrier
        barrier.await();
        // make the test worker wait until all request threads complete their work.
        latch.await();

        List<Boolean> actual = resultsQueue.stream().map(RateLimitResults::isAllowed).collect(Collectors.toList());
        List<Boolean> expected = Arrays.asList(true, true, true, false);
        assertThat("Should contain results in any order", actual, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void isAllowed_should_return_correct_num_seconds_until_window_reset_when_reject() throws InterruptedException {
        String userId = "1234";
        List<Boolean> expectedFrom3Invocations = Arrays.asList(true, true, true);
        List<Boolean> resultsFrom3Invocations = invokeIsAllowed(userId, 3)
                .stream()
                .map(RateLimitResults::isAllowed).collect(Collectors.toList());
        assertThat(resultsFrom3Invocations, is(expectedFrom3Invocations));

        // wait for 2 seconds
        Thread.sleep(2000);

        // invoke again
        RateLimitResults results = rateLimiter.isAllowed(userId);
        assertFalse(results.isAllowed());
        assertThat(results.getNumOfSecondsForRetry(), is(2L));
    }

    private List<RateLimitResults> invokeIsAllowed(String userId, int numberOfTimes) {
        List<RateLimitResults> resultsList = new ArrayList<>();
        for (int i = 0; i < numberOfTimes; i++) {
            resultsList.add(rateLimiter.isAllowed(userId));
        }
        return resultsList;
    }
}
