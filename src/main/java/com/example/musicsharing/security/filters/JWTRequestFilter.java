package com.example.musicsharing.security.filters;

import com.example.musicsharing.models.dto.ErrorDetail;
import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.security.AttemptsLimitService;
import com.example.musicsharing.security.ResponseWrapper;
import com.example.musicsharing.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.micrometer.common.lang.NonNullApi;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@NonNullApi
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JWTRequestFilter extends OncePerRequestFilter {

    //@TODO One good filter instead two bad

    JWTUtil jwtUtil;
    AttemptsLimitService attemptsLimitService;
    UserRepository userRepository;

    static String JWT_EXPIRED_MESSAGE = "Token is expired.";
    static String JWT_INVALID_MESSAGE = "Token is invalid.";

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String uri = request.getRequestURI();
            String jwtToken = header.substring(7);

            try {
                if (uri.endsWith("reset-password")) {
                    doFilterOnResetPassword(jwtToken);
                } else {
                    doRegularFilter(jwtToken);
                }
            } catch (JwtException ex) {
                catchFailureAttempt(request);
                handleJwtException(response, ex);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }


    private static void handleJwtException(HttpServletResponse response, JwtException ex) {
        ErrorDetail errorDetail = new ErrorDetail("token", null);
        if (ex instanceof ExpiredJwtException) {
            errorDetail.setMessage(JWT_EXPIRED_MESSAGE);
        } else {
            errorDetail.setMessage(JWT_INVALID_MESSAGE);
        }
        ResponseWrapper.generateAuthFailureResponse(response, errorDetail);
    }


    private void catchFailureAttempt(HttpServletRequest request) {
        if (request.getRequestURI().contains("/reset-password")) {
            String ipAddress = request.getRemoteAddr();
            String identifier = "IP: " + ipAddress;
            attemptsLimitService.incrementLoginAttempts(identifier);
        }
    }


    private void doFilterOnResetPassword(String jwtToken) {
        String subject = jwtUtil.extractClaim("sub", jwtToken);
        Long userId = Long.parseLong(subject);
        UsernamePasswordAuthenticationToken authToken;
        try {
            User user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
            authToken = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), null,
                    Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
            );
        } catch (EntityNotFoundException e) {
            throw new MalformedJwtException(JWT_INVALID_MESSAGE);
        }
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }


    private void doRegularFilter(String jwtToken) {
        String username = jwtUtil.extractClaim("username", jwtToken);
        if (!userRepository.existsByUsername(username)) {
            throw new MalformedJwtException("Jwt token with unknown username " + username);
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String role = jwtUtil.extractClaim("role", jwtToken);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singleton(new SimpleGrantedAuthority(role))
            );
            SecurityContextHolder.getContext().setAuthentication(token);
        }
    }
}
