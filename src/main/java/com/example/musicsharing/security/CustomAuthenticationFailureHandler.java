package com.example.musicsharing.security;

import com.example.musicsharing.models.dto.ErrorDetail;
import com.example.musicsharing.models.dto.LoginDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    AttemptsLimitService attemptsLimitService;

    static String IDENTIFIER_PREFIX = "Username: %s";
    static String FAILURE_MESSAGE = "Bad credentials";


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) {
        catchFailureAttempt(request);
        ErrorDetail errorDetail = new ErrorDetail("authentication", FAILURE_MESSAGE);
        ResponseWrapper.generateAuthFailureResponse(response, errorDetail);
    }


    private void catchFailureAttempt(HttpServletRequest request) {
        LoginDTO loginDTO = (LoginDTO) request.getAttribute("loginData");
        String username = loginDTO.getUsername();
        String identifier = String.format(IDENTIFIER_PREFIX, username);
        attemptsLimitService.incrementLoginAttempts(identifier);
    }
}
