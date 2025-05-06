package com.example.musicsharing.cache;

public class CacheServiceException extends RuntimeException {
    public CacheServiceException(String message) {
        super(message);
    }
    public CacheServiceException(String message, Throwable cause) {
        super(message);
    }
    public CacheServiceException(Throwable cause) {
        super(cause);
    }
}
