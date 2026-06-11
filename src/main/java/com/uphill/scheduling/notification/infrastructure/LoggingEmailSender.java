package com.uphill.scheduling.notification.infrastructure;

import com.uphill.scheduling.notification.domain.EmailMessage;
import com.uphill.scheduling.notification.domain.EmailSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default {@link EmailSender}: writes the message to the log instead of touching
 * any mail server. Active unless {@code scheduling.notifications.transport} is set
 * to something other than {@code log} (the property defaults to {@code log}, hence
 * {@code matchIfMissing = true}).
 *
 * <p>This lets the service start and exercise the full booking → event → notify
 * flow with zero external dependencies (no SMTP, no credentials), which is exactly
 * the "mock / fake SMTP is acceptable" allowance in the brief — while the real
 * {@link SmtpEmailSender} sits behind the same port for production.
 */
@Component
@ConditionalOnProperty(name = "scheduling.notifications.transport", havingValue = "log", matchIfMissing = true)
class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(EmailMessage message) {
        log.info("""
                [EMAIL] (transport=log — not actually sent)
                  To:      {}
                  Subject: {}
                  Body:
                {}""",
                message.to(), message.subject(), indent(message.body()));
    }

    private static String indent(String body) {
        return body.lines().map(line -> "    " + line).reduce((a, b) -> a + "\n" + b).orElse("");
    }
}
