package com.westminster.smartcampus.exception;

/**
 * Thrown when attempting to delete a Room that still has sensors assigned.
 * Mapped to HTTP 409 Conflict by ApiExceptionMapper.
 */
public class RoomNotEmptyException extends ApiException {

    public RoomNotEmptyException(String message) {
        super("CONFLICT", message);
    }
}
