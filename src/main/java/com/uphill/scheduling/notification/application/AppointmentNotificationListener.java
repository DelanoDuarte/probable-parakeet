package com.uphill.scheduling.notification.application;

import com.uphill.scheduling.appointment.AppointmentBooked;
import com.uphill.scheduling.notification.domain.EmailMessage;
import com.uphill.scheduling.notification.domain.EmailSender;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Sends the patient their confirmation e-mail once an appointment is booked.
 *
 * <p>This is one of three independent post-booking side effects. It is annotated
 * with {@link ApplicationModuleListener}, which combines {@code @Async},
 * {@code @Transactional} and {@code @TransactionalEventListener(AFTER_COMMIT)}.
 * Concretely that means:
 * <ul>
 *   <li>it only runs <em>after</em> the booking transaction has committed, so we
 *       never e-mail a patient about an appointment that was rolled back;</li>
 *   <li>it runs on its own thread and in its own transaction, decoupled from the
 *       request that created the booking;</li>
 *   <li>backed by Spring Modulith's persistent Event Publication Registry, the
 *       event is stored before delivery and only marked complete on success — so
 *       a crash mid-delivery leaves an incomplete publication that can be
 *       resubmitted (at-least-once delivery), rather than a silently lost email.</li>
 * </ul>
 */
@Component
class AppointmentNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentNotificationListener.class);

    private static final DateTimeFormatter HUMAN_READABLE =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'at' HH:mm", Locale.forLanguageTag("pt-PT"))
                    .withZone(ZoneId.of("Europe/Lisbon"));

    private final EmailSender emailSender;

    AppointmentNotificationListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @ApplicationModuleListener
    void on(AppointmentBooked event) {
        log.info("Preparing confirmation e-mail for appointment {} to {}",
                event.appointmentId(), event.patientEmail());

        EmailMessage message = new EmailMessage(
                event.patientEmail(),
                "Your appointment is confirmed",
                buildBody(event));

        emailSender.send(message);
    }

    private String buildBody(AppointmentBooked event) {
        String when = HUMAN_READABLE.format(event.start());
        return """
                Dear %s,

                Your %s appointment has been confirmed.

                  When:   %s
                  Doctor: %s
                  Room:   %s

                Please arrive 10 minutes early. If you need to cancel or reschedule,
                reply to this e-mail or contact our front desk.

                Kind regards,
                UpHill Health Scheduling
                """.formatted(
                event.patientName(),
                event.specialty().displayName(),
                when,
                event.doctorName(),
                event.roomName());
    }
}
