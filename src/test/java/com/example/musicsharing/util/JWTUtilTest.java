package com.example.musicsharing.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JWTUtilTest {

    public JWTUtil jwtUtil = new JWTUtil();
    private final String secret = "bhvCCRctYVTvYTVVtyCCc76rrFB67T6B766TB6B66bt6bt6b6";

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
    }

    @Test
    void generateToken_shouldReturnToken() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        String token = jwtUtil.generateToken("1", new HashMap<>(), Duration.ofSeconds(5));

        String expectedId = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("sub", String.class);

        assertNotNull(token);
        assertEquals("1", expectedId);

    }


    @Test
    void extractClaim_shouldReturnClaim() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "1");
        String token = jwtUtil.generateToken("1", claims, Duration.ofSeconds(5));

        Long expectedId = Long.valueOf(jwtUtil.extractClaim("sub", token));

        assertEquals(1, expectedId);
    }


    @Test
    void isTokenValid() {
        String token = jwtUtil.generateToken("1", Map.of("sub", "1"), Duration.ofSeconds(5));
        boolean valid = jwtUtil.isTokenValid(token);

        assertTrue(valid);

        token = token.concat("abc");
        valid = jwtUtil.isTokenValid(token);

        assertFalse(valid);
    }
}