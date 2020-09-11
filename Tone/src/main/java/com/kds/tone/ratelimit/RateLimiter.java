package com.kds.tone.ratelimit;

import com.kds.tone.model.RateLimitResults;

public interface RateLimiter {

    RateLimitResults isAllowed(final String userId);

}
