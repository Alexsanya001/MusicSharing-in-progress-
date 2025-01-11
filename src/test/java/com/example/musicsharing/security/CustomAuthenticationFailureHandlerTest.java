package com.example.musicsharing.security;

import com.example.musicsharing.models.dto.ErrorDetail;
import com.example.musicsharing.models.dto.LoginDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationFailureHandlerTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private AuthenticationException exception;
    @Mock
    private AttemptsLimitService limitService;

    @InjectMocks
    private CustomAuthenticationFailureHandler failureHandler;

    @Test
    void onAuthenticationFailure() {
        LoginDTO loginDTO = LoginDTO.builder()
                .username("username")
                .password("password")
                .build();
        when(request.getAttribute("loginData")).thenReturn(loginDTO);

        try(var mockedStatic = mockStatic(ResponseWrapper.class)){

            failureHandler.onAuthenticationFailure(request, response, exception);

            ArgumentCaptor<ErrorDetail> captor = ArgumentCaptor.forClass(ErrorDetail.class);
            mockedStatic.verify(() ->
                    ResponseWrapper.generateAuthFailureResponse(eq(response), captor.capture()));
            ErrorDetail errorDetail = captor.getValue();

            assertEquals("authentication", errorDetail.getField());
            assertEquals("Bad credentials", errorDetail.getMessage());
            verify(limitService).incrementLoginAttempts("Username: username");
        }
    }
}