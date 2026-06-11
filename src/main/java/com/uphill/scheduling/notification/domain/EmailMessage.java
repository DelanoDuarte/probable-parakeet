package com.uphill.scheduling.notification.domain;

/**
 * An immutable, transport-agnostic representation of an outbound e-mail.
 *
 * <p>Keeping this as a small value object (rather than leaking Jakarta Mail's
 * {@code MimeMessage} into the application layer) means the notification logic
 * can be unit-tested without any mail infrastructure, and the concrete
 * {@link EmailSender} implementation is free to map it onto SMTP, a transactional
 * e-mail API (SendGrid, SES, ...), or a log line.
 */
public record EmailMessage(String to, String subject, String body) {

    public EmailMessage {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("e-mail recipient must not be blank");
        }
        if (subject == null) {
            subject = "";
        }
        if (body == null) {
            body = "";
        }
    }
}
