package com.example.musicsharing.security;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class CustomAuthenticationSuccessHandlerTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;
    @Mock
    ObjectMapper mapper;
    @Mock
    JWTUtil jwtUtil;
    @Mock
    AttemptsLimitService limitService;
    @Mock
    PrintWriter writer;

    @InjectMocks
    CustomAuthenticationSuccessHandler handler;

    final static long tokenExpiration = 10L;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(handler, "tokenExpTime", Duration.ofMinutes(tokenExpiration));
    }

    @Test
    void onAuthenticationSuccess_shouldGenerateTokenAndReturnResponse() throws Exception {
        CustomUserDetails user = new CustomUserDetails(
                1L, "username", "password",
                Collections.singleton(new SimpleGrantedAuthority("USER")));

        String expectedToken = "expected-token";

        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtUtil.generateToken(eq("1"), anyMap(), eq(Duration.ofMinutes(tokenExpiration))))
                .thenReturn(expectedToken);
        when(mapper.writeValueAsString(any(ApiResponse.class)))
                .thenReturn("{\"isSuccess\":true,\"data\":\"generated-token\", \"errors\": null}");
        when(response.getWriter()).thenReturn(writer);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(jwtUtil).generateToken(eq("1"), anyMap(), eq(Duration.ofMinutes(tokenExpiration)));
        verify(writer).write("{\"isSuccess\":true,\"data\":\"generated-token\", \"errors\": null}");
        verify(limitService).discardLoginAttempts("Username: username");
    }
}