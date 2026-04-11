package com.westminster.smartcampus.exception;

/**
 * Thrown when a client tries to interact with a sensor that is not available for use.
 *
 * Mapped to 403 Forbidden in {@link com.westminster.smartcampus.mapper.ApiExceptionMapper}.
 */
public class SensorUnavailableException extends ApiException {

    public SensorUnavailableException(String message) {
        super("FORBIDDEN", message);
    }
}
