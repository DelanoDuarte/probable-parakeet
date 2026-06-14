package com.uphill.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Periodically resubmits event publications that remain OPEN — i.e. whose listener
 * threw an exception (external system down, transient network failure, etc.).
 *
 * <p>Spring Modulith's EPR guarantees at-least-once delivery, but without this
 * scheduler the retry only happens on application restart. This runs every
 * {@value RETRY_INTERVAL_MS} ms and resubmits events older than
 * {@value IN_FLIGHT_THRESHOLD_SECONDS}s, giving in-flight async listeners time
 * to complete before treating a publication as failed.
 *
 * <p>Each listener gets its own EPR entry so failures are independent: if the
 * doctor-calendar API is down but the room-reservation API is up, only the
 * calendar publications stay OPEN and get retried; room reservations are not
 * re-attempted.
 */
@Component
class EventRepublicationScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventRepublicationScheduler.class);

    private static final long RETRY_INTERVAL_MS = 2 * 60 * 1000L;
    private static final int IN_FLIGHT_THRESHOLD_SECONDS = 60;

    private final IncompleteEventPublications incompletePublications;

    EventRepublicationScheduler(IncompleteEventPublications incompletePublications) {
        this.incompletePublications = incompletePublications;
    }

    @Scheduled(fixedDelay = RETRY_INTERVAL_MS)
    void retryIncomplete() {
        log.debug("Checking for incomplete event publications to resubmit");
        incompletePublications.resubmitIncompletePublicationsOlderThan(
                Duration.ofSeconds(IN_FLIGHT_THRESHOLD_SECONDS));
    }
}
