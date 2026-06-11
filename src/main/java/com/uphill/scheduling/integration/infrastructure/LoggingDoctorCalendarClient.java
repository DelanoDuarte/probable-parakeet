package com.uphill.scheduling.integration.infrastructure;

import com.uphill.scheduling.integration.domain.DoctorCalendarClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Stand-in {@link DoctorCalendarClient} that logs the call it <em>would</em> make.
 *
 * <p>It deliberately mimics the shape of a real integration: in production the body
 * below would build a request and POST it to the calendar API, e.g.
 * <pre>{@code
 *   restClient.post()
 *       .uri("/calendars/{doctorId}/blocks", doctorId)
 *       .body(new BlockRequest(start, end))
 *       .retrieve()
 *       .toBodilessEntity();
 * }</pre>
 * On a non-2xx / IO failure it would throw {@code ExternalSystemException}, which
 * the listener lets propagate so the event publication is retried.
 */
@Component
class LoggingDoctorCalendarClient implements DoctorCalendarClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingDoctorCalendarClient.class);

    @Override
    public void blockSlot(UUID doctorId, String doctorName, Instant start, Instant end) {
        log.info("[EXTERNAL → DoctorCalendar] PUT /calendars/{}/blocks  doctor='{}' {} … {}  (simulated)",
                doctorId, doctorName, start, end);
        // Real implementation: perform HTTP call; on failure throw ExternalSystemException.
    }
}
