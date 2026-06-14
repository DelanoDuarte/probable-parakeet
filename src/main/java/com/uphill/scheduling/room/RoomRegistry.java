package com.uphill.scheduling.room;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the room module: a read-only directory of consultation rooms.
 * Time-based availability is decided by the scheduling module.
 */
public interface RoomRegistry {

    List<RoomView> findAllActive();

    Optional<RoomView> findById(UUID id);
}
