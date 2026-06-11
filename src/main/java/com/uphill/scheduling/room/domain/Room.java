package com.uphill.scheduling.room.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Room aggregate root: a physical consultation room that can be reserved. */
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected Room() {
        // for JPA
    }

    private Room(UUID id, String name, boolean active) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name must not be blank");
        }
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public static Room create(String name) {
        return new Room(UUID.randomUUID(), name, true);
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
