package com.uphill.scheduling.appointment.infrastructure;

import com.uphill.scheduling.appointment.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Spring Data JPA repository. Kept package-private and wrapped by
 * {@link AppointmentRepositoryAdapter} so the domain port never leaks Spring
 * Data types.
 */
interface AppointmentJpaRepository extends JpaRepository<Appointment, UUID> {

    @Query("""
            select a.doctorId from Appointment a
            where a.status = com.uphill.scheduling.appointment.domain.AppointmentStatus.CONFIRMED
              and a.slot.startAt < :end and a.slot.endAt > :start
            """)
    Set<UUID> findBusyDoctorIds(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select a.roomId from Appointment a
            where a.status = com.uphill.scheduling.appointment.domain.AppointmentStatus.CONFIRMED
              and a.slot.startAt < :end and a.slot.endAt > :start
            """)
    Set<UUID> findBusyRoomIds(@Param("start") Instant start, @Param("end") Instant end);
}
