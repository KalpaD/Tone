package com.kds.tone.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RateLimitResponse {

    @JsonProperty
    private boolean allowed;
    @JsonProperty
    private String message;

    public RateLimitResponse(boolean allowed) {
        this.allowed = allowed;
    }

    public RateLimitResponse(boolean allowed, String message) {
        this.allowed = allowed;
        this.message = message;
    }
}
