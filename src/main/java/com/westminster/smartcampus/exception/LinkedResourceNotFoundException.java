package com.westminster.smartcampus.exception;

/**
 * Thrown when a request references another resource that does not exist (e.g., sensor.roomId missing).
 * Mapped to HTTP 422 Unprocessable Entity by ApiExceptionMapper.
 */
public class LinkedResourceNotFoundException extends ApiException {

    public LinkedResourceNotFoundException(String message) {
        super("UNPROCESSABLE_ENTITY", message);
    }
}
