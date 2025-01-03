package com.example.musicsharing.security.filters;

import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.security.CustomAuthenticationFailureHandler;
import com.example.musicsharing.security.CustomAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@RequiredArgsConstructor
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final String LOGIN_URL = "/api/auth/login";

    private final CustomAuthenticationSuccessHandler authSuccessHandler;
    private final CustomAuthenticationFailureHandler authFailureHandler;


    @Override
    public void afterPropertiesSet() {
        setAuthenticationSuccessHandler(authSuccessHandler);
        setAuthenticationFailureHandler(authFailureHandler);
        setFilterProcessesUrl(LOGIN_URL);
    }


    @Override
    protected String obtainUsername(HttpServletRequest request) {
        LoginDTO loginDTO = (LoginDTO) request.getAttribute("loginData");
        return loginDTO.getUsername();
    }


    @Override
    protected String obtainPassword(HttpServletRequest request) {
        LoginDTO loginDTO = (LoginDTO) request.getAttribute("loginData");
        return loginDTO.getPassword();
    }
}
