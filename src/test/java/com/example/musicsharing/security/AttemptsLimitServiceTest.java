package com.example.musicsharing.security;

import com.example.musicsharing.services.MailService;
import com.example.musicsharing.util.RequestDataExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AttemptsLimitServiceTest {

    @Mock
    StringRedisTemplate redisTemplate;
    @Mock
    ValueOperations<String, String> valueOperations;
    @Mock
    RequestDataExtractor dataExtractor;
    @Mock
    HttpServletRequest request;
    @Mock
    ApplicationEventPublisher eventPublisher;

    private AttemptsLimitService attemptsLimitService;

    private static final String recordPrefix = "Authentication failure | ";
    private static final String identifier = "Username: testUser";
    private static final String key = recordPrefix + identifier;
    private static final String ipAddress = "192.168.1.1";


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        attemptsLimitService = new AttemptsLimitService(redisTemplate, dataExtractor, eventPublisher);
    }


    @Test
    void isNotAllowed_shouldReturnFalse_whenRequestIsAllowed() {
        when(valueOperations.get(key)).thenReturn("4");

        boolean isForbidden = attemptsLimitService.isNotAllowed(identifier);

        assertFalse(isForbidden);
        verify(valueOperations).get(key);
    }


    @Test
    void isNotAllowed_shouldReturnTrue_whenRequestIsForbidden() {
        when(valueOperations.get(key)).thenReturn("5");

        boolean isForbidden = attemptsLimitService.isNotAllowed(identifier);

        assertTrue(isForbidden);
        verify(valueOperations).get(key);
    }


    @Test
    void incrementLoginAttempts_shouldIncrementAndSetExpiration_whenIsFirstAttempt() {
        when(valueOperations.increment(key)).thenReturn(1L);

        attemptsLimitService.incrementLoginAttempts(identifier);

        verify(valueOperations).increment(key);
        verify(redisTemplate).expire(key, Duration.ofMinutes(5));
    }


    @Test
    void incrementLoginAttempts_shouldIncrementAttempt_whenIsNotFirstAttempt() {
        when(valueOperations.increment(key)).thenReturn(2L);

        attemptsLimitService.incrementLoginAttempts(identifier);

        verify(valueOperations).increment(key);
        verify(redisTemplate, never()).expire(key, Duration.ofMinutes(5));
    }


    @Test
    void discardLoginAttempts_shouldDeleteKey() {
        attemptsLimitService.discardLoginAttempts(identifier);
        verify(redisTemplate).delete(key);
    }


    @Test
    void prepareSuspiciousAttempt_withUsername() {
        when(request.getRemoteAddr()).thenReturn(ipAddress);

        attemptsLimitService.prepareSuspiciousAttempt(request, identifier);

        String expectedIdentifier = identifier + " | IP: " + ipAddress;
        SuspiciousAttemptEvent event = new SuspiciousAttemptEvent(expectedIdentifier);
        verify(eventPublisher).publishEvent(eq(event));
    }


    @Test
    void prepareSuspiciousAttempt_withIpAddress() {
        String identifier = "IP: " + ipAddress;
        when(dataExtractor.extractUserId(request)).thenReturn("1");

        attemptsLimitService.prepareSuspiciousAttempt(request, identifier);

        String expectedIdentifier = "UserId: 1 | " + identifier;
        SuspiciousAttemptEvent event = new SuspiciousAttemptEvent(expectedIdentifier);
        verify(eventPublisher).publishEvent(eq(event));
    }
}