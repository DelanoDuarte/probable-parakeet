package com.uphill.scheduling.room.infrastructure;

import com.uphill.scheduling.room.domain.Room;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
class RoomSeeder {

    @Bean
    ApplicationRunner seedRooms(RoomJpaRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.saveAll(List.of(
                    Room.create("Room 101"),
                    Room.create("Room 102"),
                    Room.create("Room 103"),
                    Room.create("Room 104"),
                    Room.create("Room 105")
            ));
        };
    }
}
