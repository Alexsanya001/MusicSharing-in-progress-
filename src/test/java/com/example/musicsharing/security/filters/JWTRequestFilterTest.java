package com.example.musicsharing.security.filters;

import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.security.AttemptsLimitService;
import com.example.musicsharing.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTRequestFilterTest {

    private JWTRequestFilter jwtRequestFilter;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    UserRepository userRepository;
    @Mock
    AttemptsLimitService attemptsLimitService;


    @BeforeEach
    public void setUp() {
        jwtRequestFilter = new JWTRequestFilter(jwtUtil, attemptsLimitService, userRepository);
        SecurityContextHolder.setContext(new SecurityContextImpl());
    }

    @Test
    void shouldNotSetAuthentication_whenTokenIsNull() throws Exception {

        when(request.getHeader("Authorization")).thenReturn(null);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    void shouldNotSetAuthentication_whenTokenIsInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("invalidToken");

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    void shouldSetAuthentication_whenTokenIsValid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(request.getRequestURI()).thenReturn("/login");
        when(jwtUtil.extractClaim("username", "validToken")).thenReturn("user");
        when(jwtUtil.extractClaim("role", "validToken")).thenReturn("ROLE_USER");
        when(userRepository.existsByUsername("user")).thenReturn(true);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }


    @Test
    void shouldReturnUnauthorized_whenTokenIsExpired() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer expiredToken");
        when(request.getRequestURI()).thenReturn("/mock-uri");
        doThrow(new ExpiredJwtException(null, null, "Token expired"))
                .when(jwtUtil).extractClaim("username", "expiredToken");

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("Token is expired."));
        verifyNoInteractions(filterChain);
    }


    @Test
    void shouldReturnUnauthorized_whenTokenIsNotValid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        when(request.getRequestURI()).thenReturn("/reset-password");
        doThrow(new MalformedJwtException("Token is not valid"))
                .when(jwtUtil).extractClaim("sub", "invalidToken");

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("Token is invalid."));
        verifyNoInteractions(filterChain);
    }
}