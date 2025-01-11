package com.example.musicsharing.security.filters;

import com.example.musicsharing.models.dto.ErrorDetail;
import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.models.dto.RestorePasswordDto;
import com.example.musicsharing.security.AttemptsLimitService;
import com.example.musicsharing.security.ResponseWrapper;
import com.example.musicsharing.util.RequestDataExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AttemptsLimitFilterTest {

    @Mock
    AttemptsLimitService limitService;
    @Mock
    RequestDataExtractor dataExtractor;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain filterChain;

    @InjectMocks
    private AttemptsLimitFilter attemptsLimitFilter;


    @Test
    void doFilter_shouldBlockRequest_whenLimitExceeded() throws Exception {
        LoginDTO loginDTO = LoginDTO.builder()
                .username("username")
                .password("password")
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(loginDTO);

        try (BufferedReader reader = new BufferedReader(new StringReader(requestBody))) {
            when(request.getReader()).thenReturn(reader);
            when(request.getRequestURI()).thenReturn("/api/auth/login");
            when(dataExtractor.extractLoginData(any())).thenReturn(loginDTO);
            when(limitService.isNotAllowed("Username: " + loginDTO.getUsername()))
                    .thenReturn(true);

            try (var mockedStatic = mockStatic(ResponseWrapper.class)) {

                attemptsLimitFilter.doFilter(request, response, filterChain);

                verify(limitService).prepareSuspiciousAttempt(any(), eq("Username: " + loginDTO.getUsername()));

                ArgumentCaptor<ErrorDetail> captor = ArgumentCaptor.forClass(ErrorDetail.class);
                mockedStatic.verify(() ->
                        ResponseWrapper.generateAuthFailureResponse(eq(response), captor.capture()));
                ErrorDetail errorDetail = captor.getValue();

                assertEquals("authentication", errorDetail.getField());
                assertEquals("Too many attempts", errorDetail.getMessage());
                verifyNoInteractions(filterChain);
            }
        }
    }


    @Test
    void doFilter_shouldNotBlockRequest_whenLimitNotExceeded() throws Exception {
        RestorePasswordDto restorePasswordDto = RestorePasswordDto.builder().newPassword("NewPassword1").build();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(restorePasswordDto);
        String ipAddress = "127.0.0.1";

        try (BufferedReader reader = new BufferedReader(new StringReader(requestBody))) {
            when(request.getReader()).thenReturn(reader);
            when(request.getRequestURI()).thenReturn("/api/auth/reset-password");
            when(request.getRemoteAddr()).thenReturn(ipAddress);
            when(limitService.isNotAllowed("IP: " + ipAddress))
                    .thenReturn(false);

            attemptsLimitFilter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(any(HttpServletRequest.class), eq(response));
            verify(limitService, never()).prepareSuspiciousAttempt(any(HttpServletRequest.class), anyString());
            verifyNoInteractions(response);
        }
    }
}