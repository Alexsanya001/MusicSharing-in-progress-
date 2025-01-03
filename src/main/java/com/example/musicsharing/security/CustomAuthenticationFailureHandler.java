package com.example.musicsharing.security;

import com.example.musicsharing.models.dto.LoginDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final AttemptsLimitService attemptsLimitService;
    private static final String IDENTIFIER_PREFIX = "Username: %s";
    public static final String AUTHENTICATION_FAILURE_RESPONSE_BODY = "{\"error\":\"Authentication failed\"}";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        catchFailureAttempt(request);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(AUTHENTICATION_FAILURE_RESPONSE_BODY);
        response.getWriter().flush();
    }


    private void catchFailureAttempt(HttpServletRequest request) {
        LoginDTO loginDTO = (LoginDTO) request.getAttribute("loginData");
        String username = loginDTO.getUsername();
        String identifier = String.format(IDENTIFIER_PREFIX, username);
        attemptsLimitService.incrementLoginAttempts(identifier);
    }
}
