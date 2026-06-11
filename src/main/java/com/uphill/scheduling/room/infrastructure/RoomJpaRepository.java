package com.uphill.scheduling.room.infrastructure;

import com.uphill.scheduling.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface RoomJpaRepository extends JpaRepository<Room, UUID> {

    List<Room> findByActiveIsTrueOrderByNameAsc();
}
