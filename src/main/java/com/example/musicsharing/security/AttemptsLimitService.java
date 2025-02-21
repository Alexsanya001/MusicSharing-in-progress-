package com.example.musicsharing.security;

import com.example.musicsharing.util.RequestDataExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AttemptsLimitService {

    static int MAX_ATTEMPTS = 5;
    static Duration TIMEOUT = Duration.ofMinutes(5);
    static String RECORD_PREFIX = "Authentication failure | ";
    static String USERID_PREFIX = "UserId: %s | %s";
    static String IP_PREFIX = " | IP: ";

    StringRedisTemplate redisTemplate;
    RequestDataExtractor extractor;
    ApplicationEventPublisher eventPublisher;


    public boolean isNotAllowed(String identifier) {
        String key = RECORD_PREFIX + identifier;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && Long.parseLong(attempts) >= MAX_ATTEMPTS;
    }

    @Async
    public void incrementLoginAttempts(String identifier) {
        String key = RECORD_PREFIX + identifier;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1L) {
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
            String userId = extractor.extractUserId(request);
            identifier = String.format(USERID_PREFIX, userId, identifier);
        }
        eventPublisher.publishEvent(new SuspiciousAttemptEvent(identifier));
    }
}
