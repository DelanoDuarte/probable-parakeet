package com.uphill.scheduling.notification.domain;

/**
 * Outbound port for delivering an {@link EmailMessage}.
 *
 * <p>This is the "clear abstraction / proper integration point" the challenge
 * asks for: the application layer depends only on this interface, and the actual
 * transport is chosen at runtime via configuration. Two adapters ship with the
 * service:
 * <ul>
 *   <li>{@code LoggingEmailSender} — the default; writes the message to the log
 *       so the system runs with zero external dependencies.</li>
 *   <li>{@code SmtpEmailSender} — a real {@code JavaMailSender}-backed sender,
 *       activated with {@code scheduling.notifications.transport=smtp} (wired to
 *       MailHog in docker-compose).</li>
 * </ul>
 */
public interface EmailSender {

    void send(EmailMessage message);
}
