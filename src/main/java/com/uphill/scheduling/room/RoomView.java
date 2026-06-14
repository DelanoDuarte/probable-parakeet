package com.uphill.scheduling.room;

import java.util.UUID;

/** Immutable read-model of a consultation room, exposed to other modules. */
public record RoomView(UUID id, String name) {
}
