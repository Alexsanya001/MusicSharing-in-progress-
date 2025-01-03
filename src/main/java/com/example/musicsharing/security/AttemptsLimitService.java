package com.example.musicsharing.security;

import com.example.musicsharing.services.MailService;
import com.example.musicsharing.util.RequestDataExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Log4j2
@Service
@RequiredArgsConstructor
public class AttemptsLimitService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration TIMEOUT = Duration.ofMinutes(5);
    public static final String RECORD_PREFIX = "Authentication failure | ";
    public static final String USERID_PREFIX = "UserId: %s | %s";
    public static final String IP_PREFIX = " | IP: ";
    public static final String MESSAGE = "Too many attempts for identifier: %s";

    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;
    private final RequestDataExtractor extractor;

    @Value("${admin.email}")
    private String adminEmail;


    public boolean isNotAllowed(String identifier) {
        String key = RECORD_PREFIX + identifier;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && Long.parseLong(attempts) >= MAX_ATTEMPTS;
    }

    public void incrementLoginAttempts(String identifier) {
        String key = RECORD_PREFIX + identifier;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts == 1) {
            redisTemplate.expire(key, TIMEOUT);
        }
    }

    @Async
    public void discardLoginAttempts(String identifier) {
        String key = RECORD_PREFIX + identifier;
        redisTemplate.delete(key);
    }

    @Async
    public void prepareSuspiciousAttempt(HttpServletRequest request, String identifier) {
        if (identifier.startsWith("Username")) {
            String ipAddress = request.getRemoteAddr();
            identifier += IP_PREFIX + ipAddress;
        } else {
            incrementLoginAttempts(identifier);
            String userId = extractor.extractUserId(request);
            identifier = String.format(USERID_PREFIX, userId, identifier);
        }
        logExceededAttempts(identifier);
    }

    private void logExceededAttempts(String identifier) {
        String message = String.format(MESSAGE, identifier);
        log.warn(message);
        mailService.sendMail(adminEmail, "Suspicious attempt", message);
    }
}
