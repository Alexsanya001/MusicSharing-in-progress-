package com.example.musicsharing.security.filters;

import com.example.musicsharing.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JWTRequestFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String username;
        String jwtToken;

        if (header != null && header.startsWith("Bearer ")) {

            jwtToken = header.substring(7);

            try {
                username = jwtUtil.extractClaim("username", jwtToken);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String role = jwtUtil.extractClaim("role", jwtToken);
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singleton(new SimpleGrantedAuthority(role))
                    );

                    SecurityContextHolder.getContext().setAuthentication(token);
                }
            } catch (JwtException ex) {
                handleJwtException(response, ex);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }


    private void handleJwtException(HttpServletResponse response, JwtException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> responseBody = new HashMap<>();
        String message;

        if (ex instanceof ExpiredJwtException) {
            message = "Authorization expired. Please login again.";
        } else {
            message = "Authorization failed";
        }
        responseBody.put("message", message);

        response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
    }

}
