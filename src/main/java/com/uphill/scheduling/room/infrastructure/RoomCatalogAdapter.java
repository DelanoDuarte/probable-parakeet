package com.uphill.scheduling.room.infrastructure;

import com.uphill.scheduling.room.RoomCatalog;
import com.uphill.scheduling.room.RoomSnapshot;
import com.uphill.scheduling.room.domain.Room;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Implements the room module's public {@link RoomCatalog} over JPA. */
@Component
class RoomCatalogAdapter implements RoomCatalog {

    private final RoomJpaRepository repository;

    RoomCatalogAdapter(RoomJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RoomSnapshot> findAllActive() {
        return repository.findByActiveIsTrueOrderByNameAsc()
                .stream()
                .map(RoomCatalogAdapter::toSnapshot)
                .toList();
    }

    @Override
    public Optional<RoomSnapshot> findById(UUID id) {
        return repository.findById(id).map(RoomCatalogAdapter::toSnapshot);
    }

    private static RoomSnapshot toSnapshot(Room room) {
        return new RoomSnapshot(room.getId(), room.getName());
    }
}
