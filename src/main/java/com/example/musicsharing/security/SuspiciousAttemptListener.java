package com.example.musicsharing.security;

import com.example.musicsharing.services.MailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SuspiciousAttemptListener {
    Logger logger = LoggerFactory.getLogger(SuspiciousAttemptListener.class);

    static String MESSAGE = "Too many attempts for identifier: %s";

    MailService mailService;

    @Value("${admin.email}")
    @NonFinal
    String adminEmail;

    @EventListener
    public void handleSuspiciousAttempt(SuspiciousAttemptEvent event) {
        String identifier = event.identifier();
        String message = String.format(MESSAGE, identifier);
        logger.warn(message);
        mailService.sendMail(adminEmail, "Suspicious attempt", message);
    }
}