package com.example.ragchatstorage.exception;

/**
 * Exception thrown when service layer encounters business logic violations
 * HTTP Status: 422 UNPROCESSABLE_ENTITY
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

