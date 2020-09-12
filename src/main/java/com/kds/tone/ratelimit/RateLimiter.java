package com.kds.tone.ratelimit;

import com.kds.tone.model.RateLimitResults;

public interface RateLimiter {

    /**
     * This method let the the caller know if the request belong to given userId can be allowed to invoke an api resource.
     *
     * @param userId Authenticated user's unique identifier.
     * @return {@link RateLimitResults} which contains the allowed or not results.
     */
    RateLimitResults isAllowed(final String userId);

}
