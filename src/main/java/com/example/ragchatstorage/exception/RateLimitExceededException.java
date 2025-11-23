package com.example.ragchatstorage.exception;

/**
 * Exception thrown when rate limit is exceeded
 * HTTP Status: 429 TOO_MANY_REQUESTS
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}

