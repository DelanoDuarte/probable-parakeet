package com.uphill.scheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the appointment scheduling service.
 *
 * <p>The application is a <em>modular monolith</em>: each top-level package under
 * {@code com.uphill.scheduling} is a Spring Modulith application module with an
 * explicit, verified set of allowed dependencies (see {@code ModularityTests}).
 *
 * <p>{@link EnableAsync} is required so that {@code @ApplicationModuleListener}
 * post-booking actions (email, external calendar, room reservation) run
 * asynchronously, after the booking transaction commits.
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class SchedulingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulingApplication.class, args);
    }
}
