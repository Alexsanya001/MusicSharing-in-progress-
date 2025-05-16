package com.example.musicsharing.security.filters;

import com.example.musicsharing.models.dto.ErrorDetail;
import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.models.dto.RestorePasswordDto;
import com.example.musicsharing.security.AttemptsLimitService;
import com.example.musicsharing.security.ResponseWrapper;
import com.example.musicsharing.util.RequestDataExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


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
    @Mock
    ResponseWrapper responseWrapper;

    @InjectMocks
    private AttemptsLimitFilter attemptsLimitFilter;


    @Test
    void doFilter_shouldBlockRequest_whenLimitExceeded() throws Exception {
        LoginDTO loginDTO = LoginDTO.builder()
                .username("username")
                .password("password")
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] requestBody = objectMapper.writeValueAsBytes(loginDTO);

        when(request.getInputStream()).thenReturn(getServletInputStream(requestBody));
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(dataExtractor.extractLoginData(any())).thenReturn(loginDTO);
        when(limitService.isNotAllowed("Username: " + loginDTO.getUsername()))
                .thenReturn(true);


        attemptsLimitFilter.doFilter(request, response, filterChain);

        verify(limitService).prepareSuspiciousAttempt(any(), eq("Username: " + loginDTO.getUsername()));

        ArgumentCaptor<ErrorDetail> captor = ArgumentCaptor.forClass(ErrorDetail.class);
        verify(responseWrapper).generateAuthFailureResponse(eq(response), captor.capture());
        ErrorDetail errorDetail = captor.getValue();

        assertEquals("authentication", errorDetail.getField());
        assertEquals("Too many attempts", errorDetail.getMessage());
        verifyNoInteractions(filterChain);
    }


    @Test
    void doFilter_shouldNotBlockRequest_whenLimitNotExceeded() throws Exception {
        RestorePasswordDto restorePasswordDto = RestorePasswordDto.builder().newPassword("NewPassword1").build();
        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddress = "127.0.0.1";

        byte[] requestBody = objectMapper.writeValueAsBytes(restorePasswordDto);

        when(request.getInputStream()).thenReturn(getServletInputStream(requestBody));
        when(request.getRequestURI()).thenReturn("/api/auth/reset-password");
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(limitService.isNotAllowed("IP: " + ipAddress))
                .thenReturn(false);

        attemptsLimitFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(any(HttpServletRequest.class), eq(response));
        verify(limitService, never()).prepareSuspiciousAttempt(any(HttpServletRequest.class), anyString());
        verifyNoInteractions(response);

    }

    private static @NotNull ServletInputStream getServletInputStream(byte[] requestBody) {
        return new ServletInputStream() {
            private final ByteArrayInputStream inputStream = new ByteArrayInputStream(requestBody);

            @Override
            public int read() {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException();
            }
        };
    }
}