package com.uphill.scheduling.integration.infrastructure;

import com.uphill.scheduling.integration.domain.RoomReservationClient;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingRoomReservationClient implements RoomReservationClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingRoomReservationClient.class);

    @Override
    public void reserve(UUID roomId, String roomName, Instant start, Instant end) {
        log.info("[EXTERNAL → RoomReservation] POST /rooms/{}/reservations  room='{}' {} … {}  (simulated)",
                roomId, roomName, start, end);
        // Real implementation: perform HTTP call; on failure throw ExternalSystemException.
    }
}
