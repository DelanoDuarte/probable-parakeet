package com.uphill.scheduling;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Widens the Spring Modulith {@code event_publication.serialized_event} column on
 * H2. Hibernate defaults String columns to VARCHAR(255); {@code AppointmentBooked}
 * serializes to ~350 chars. Only active when the datasource URL contains "h2",
 * so it is a no-op on any production database.
 *
 * <p>{@link SmartInitializingSingleton} fires after all singleton beans are
 * instantiated — after {@code EntityManagerFactory} and its Hibernate DDL run —
 * so the table already exists when the ALTER executes.
 */
@Component
@ConditionalOnExpression("'${spring.datasource.url:}'.contains('h2')")
class EventPublicationSchemaFix implements SmartInitializingSingleton {

    private final JdbcTemplate jdbcTemplate;

    EventPublicationSchemaFix(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterSingletonsInstantiated() {
        jdbcTemplate.execute(
                "ALTER TABLE event_publication ALTER COLUMN serialized_event VARCHAR(4096)");
    }
}
