package com.example.musicsharing.util;

import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.security.CustomHttpServletRequestWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RequestDataExtractor {

    JWTUtil jwtUtil;
    ObjectMapper objectMapper;

    public LoginDTO extractLoginData(HttpServletRequest request) throws IOException {
        if (request instanceof CustomHttpServletRequestWrapper requestWrapper) {
            String body = requestWrapper.getBody();
            JsonNode jsonNode = objectMapper.readTree(body);
            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();
            return LoginDTO.builder()
                    .username(username)
                    .password(password)
                    .build();
        }
        return null;
    }


    public String extractUserId(HttpServletRequest request) {
        String userId = "Unknown";
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") ?
                authHeader.substring(7) : null;
        if (token != null) {
            try {
                userId = jwtUtil.extractClaim("sub", token);
            } catch (JwtException e) {
                log.error(e.getMessage());
            }
        }
        return userId;
    }
}
