package com.example.ragchatstorage.exception;

/**
 * Exception thrown when a duplicate resource is attempted to be created
 * HTTP Status: 409 CONFLICT
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}


