package com.kds.tone.service;

import com.kds.tone.model.RateLimitResponse;
import com.kds.tone.model.RateLimitResults;
import com.kds.tone.ratelimit.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterService.class);
    private RateLimiter rateLimiter;

    @Autowired
    public RateLimiterService(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public ResponseEntity<RateLimitResponse> isAllowed(final String userId) {
        RateLimitResults rateLimitResults = rateLimiter.isAllowed(userId);
        return rateLimitResults.isAllowed() ? createAllowedResponse() : createRejectedResponse(userId, rateLimitResults);
    }

    private ResponseEntity<RateLimitResponse> createAllowedResponse() {
        return new ResponseEntity<>(new RateLimitResponse(true), HttpStatus.OK);
    }

    private ResponseEntity<RateLimitResponse> createRejectedResponse(final String userId, final RateLimitResults rateLimitResults) {
        LOGGER.info("Rate limit exceeded for userId : {}", userId);
        return new ResponseEntity<>(new RateLimitResponse(false, rateLimitResults.getMessage()), HttpStatus.TOO_MANY_REQUESTS);
    }

}
