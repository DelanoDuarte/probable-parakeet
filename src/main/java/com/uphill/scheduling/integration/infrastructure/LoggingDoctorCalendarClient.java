package com.uphill.scheduling.integration.infrastructure;

import com.uphill.scheduling.integration.domain.DoctorCalendarClient;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
