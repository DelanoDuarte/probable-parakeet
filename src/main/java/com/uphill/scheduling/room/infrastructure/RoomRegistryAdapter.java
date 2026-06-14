package com.uphill.scheduling.room.infrastructure;

import com.uphill.scheduling.room.RoomRegistry;
import com.uphill.scheduling.room.RoomView;
import com.uphill.scheduling.room.domain.Room;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Implements the room module's public {@link RoomRegistry} over JPA. */
@Component
class RoomRegistryAdapter implements RoomRegistry {

    private final RoomJpaRepository repository;

    RoomRegistryAdapter(RoomJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RoomView> findAllActive() {
        return repository.findByActiveIsTrueOrderByNameAsc()
                .stream()
                .map(RoomRegistryAdapter::toView)
                .toList();
    }

    @Override
    public Optional<RoomView> findById(UUID id) {
        return repository.findById(id).map(RoomRegistryAdapter::toView);
    }

    private static RoomView toView(Room room) {
        return new RoomView(room.getId(), room.getName());
    }
}
