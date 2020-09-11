package com.kds.tone.model;

public class RateLimitResults {

    private boolean allowed;
    private long numOfSecondsForRetry;

    public RateLimitResults(boolean allowed, long numOfSecondsForRetry) {
        this.allowed = allowed;
        this.numOfSecondsForRetry = numOfSecondsForRetry;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getNumOfSecondsForRetry() {
        return numOfSecondsForRetry;
    }

    public String getMessage() {
        return String.format("Rate limit exceeded. Try again in %s seconds", this.numOfSecondsForRetry);
    }
}
