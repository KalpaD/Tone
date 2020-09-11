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

    @GetMapping("/allowed")
    public ResponseEntity<RateLimitResponse> isAllowed(@RequestHeader(name = "userId") String userId) {
        return rateLimiterService.isAllowed(userId);
    }
}
