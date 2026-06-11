package com.uphill.scheduling.integration.domain;

/**
 * Raised when a call to an external system (doctor calendar, room reservation)
 * fails. Propagating this out of an {@code @ApplicationModuleListener} causes the
 * Modulith event-publication registry to leave the publication incomplete, so it
 * can be retried rather than silently dropped.
 */
public class ExternalSystemException extends RuntimeException {

    public ExternalSystemException(String message) {
        super(message);
    }

    public ExternalSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
