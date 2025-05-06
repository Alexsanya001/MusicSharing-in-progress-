package com.example.musicsharing.security;

import com.example.musicsharing.cache.CacheService;
import com.example.musicsharing.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    JWTUtil jwtUtil;
    AttemptsLimitService attemptsLimitService;
    CacheService cacheService;

    @Value("${jwt.exp-time.long}")
    @NonFinal
    Duration tokenExpTime;

    static String IDENTIFIER_PREFIX = "Username: %s";


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = generateToken(userDetails);
        ResponseWrapper.generateAuthSuccessResponse(response, token);
        cacheService.put(userDetails.getUsername(), userDetails.user());
        attemptsLimitService.discardLoginAttempts(
                String.format(IDENTIFIER_PREFIX, userDetails.getUsername())
        );
    }


    private String generateToken(CustomUserDetails principal) {
        String userId = String.valueOf(principal.user().getId());
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", principal.getUsername());
        principal.getAuthorities()
                .stream()
                .findFirst()
                .ifPresent(auth -> claims.put("role", auth.getAuthority()));

        return jwtUtil.generateToken(userId, claims, tokenExpTime);
    }
}
