package com.example.musicsharing.security.filters;

import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.security.CustomAuthenticationFailureHandler;
import com.example.musicsharing.security.CustomAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    static String LOGIN_URL = "/api/auth/login";

    CustomAuthenticationSuccessHandler authSuccessHandler;
    CustomAuthenticationFailureHandler authFailureHandler;


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
