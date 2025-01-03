package com.example.musicsharing.security.filters;

import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.security.AttemptsLimitService;
import com.example.musicsharing.security.CustomHttpServletRequestWrapper;
import com.example.musicsharing.util.RequestDataExtractor;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AttemptsLimitFilter implements Filter {

    private final AttemptsLimitService attemptsLimitService;
    private final RequestDataExtractor requestDataExtractor;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            CustomHttpServletRequestWrapper requestWrapper = new CustomHttpServletRequestWrapper(httpRequest);
            String uri = httpRequest.getRequestURI();

            if (uri.contains("/reset-password") || uri.contains("/login")) {
                String destination = uri.substring(uri.lastIndexOf('/'));

                String identifier = switch (destination) {
                    case "/login" -> {
                        LoginDTO loginDTO = requestDataExtractor.extractLoginData(requestWrapper);
                        requestWrapper.setAttribute("loginData", loginDTO);
                        yield String.format("Username: %s", loginDTO.getUsername());
                    }
                    case "/reset-password" -> {
                        String ipAddress = request.getRemoteAddr();
                        yield String.format("IP: %s", ipAddress);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + uri);
                };

                if (attemptsLimitService.isNotAllowed(identifier)) {
                    attemptsLimitService.prepareSuspiciousAttempt(requestWrapper, identifier);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Too many attempts\"}");
                    response.getWriter().flush();
                    return;
                }
            }
            filterChain.doFilter(requestWrapper, response);
        }
    }
}
