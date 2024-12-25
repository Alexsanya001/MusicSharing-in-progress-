package com.example.musicsharing.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JWTUtil {

    @Value("${jwt-secret-key}")
    private String secret;
    private SecretKey key;


    public String generateToken(String sub, Map<String, String> claims, Duration expiresIn) {
        initKey();

        return Jwts.builder()
                .subject(sub)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(expiresIn)))
                .signWith(key)
                .compact();
    }


    public String extractClaim(String claim, String token) {
        initKey();

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(claim, String.class);
    }


    public boolean isTokenValid(String token) {
        initKey();

        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException e) {
            return false;
        }

        return true;
    }


    private void initKey() {
        if (key == null) {
            key = Keys.hmacShaKeyFor(secret.getBytes());
        }
    }
}
