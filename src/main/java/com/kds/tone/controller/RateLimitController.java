package com.kds.tone.controller;

import com.kds.tone.model.RateLimitResponse;
import com.kds.tone.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimitController {

    private RateLimiterService rateLimiterService;

    @Autowired
    public RateLimitController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    /**
     * This endpoint let it's caller know that if the given authenticated userId has reached the maximum number of requests.
     * Currently only support for pre-configured max number of requests within well known system wide time window.
     * <ul>
     *      <li> Clients of this API should be aware of the current rate limiting configuration before they use this to throttle their APIs.
     * </ul>
     * @param userId A system wide unique identifier. Expected as a mandatory header. This should be a system provided.
     * @return A @{link {@link RateLimitResponse}} which contains the results as a boolean and if not allowed number of seconds for the next retry.
     */
    @GetMapping("/allowed")
    public ResponseEntity<RateLimitResponse> isAllowed(@RequestHeader(name = "userId") String userId) {
        return rateLimiterService.isAllowed(userId);
    }
}
