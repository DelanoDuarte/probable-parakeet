package com.uphill.scheduling.notification.infrastructure;

import com.uphill.scheduling.notification.domain.EmailMessage;
import com.uphill.scheduling.notification.domain.EmailSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Real SMTP {@link EmailSender}, activated with
 * {@code scheduling.notifications.transport=smtp}. In docker-compose this is wired
 * to MailHog (host {@code mailhog}, port {@code 1025}), whose web UI on
 * {@code :8025} lets you actually see the confirmation e-mails — a "fake SMTP"
 * that nonetheless exercises the genuine JavaMail code path end to end.
 *
 * <p>The transport is swapped purely by configuration; no application code changes
 * between the log, MailHog, and a production relay (SES/SendGrid SMTP).
 */
@Component
@ConditionalOnProperty(name = "scheduling.notifications.transport", havingValue = "smtp")
class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;

    SmtpEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(EmailMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("no-reply@uphill-scheduling.pt");
        mail.setTo(message.to());
        mail.setSubject(message.subject());
        mail.setText(message.body());

        mailSender.send(mail);
        log.info("Confirmation e-mail dispatched to {} via SMTP", message.to());
    }
}
