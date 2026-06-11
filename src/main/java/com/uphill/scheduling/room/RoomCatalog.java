package com.uphill.scheduling.room;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the room module: the catalogue of consultation rooms.
 * As with doctors, time-based availability is decided by the scheduling module.
 */
public interface RoomCatalog {

    List<RoomSnapshot> findAllActive();

    Optional<RoomSnapshot> findById(UUID id);
}
