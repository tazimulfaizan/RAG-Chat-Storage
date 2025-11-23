package com.example.ragchatstorage.exception;

/**
 * Exception thrown when database operations fail
 * HTTP Status: 500 INTERNAL_SERVER_ERROR
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

