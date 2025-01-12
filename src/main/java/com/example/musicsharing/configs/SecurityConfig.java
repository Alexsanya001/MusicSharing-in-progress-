package com.example.musicsharing.configs;

import com.example.musicsharing.security.CustomAuthenticationFailureHandler;
import com.example.musicsharing.security.CustomAuthenticationSuccessHandler;
import com.example.musicsharing.security.filters.AttemptsLimitFilter;
import com.example.musicsharing.security.filters.CustomUsernamePasswordAuthenticationFilter;
import com.example.musicsharing.security.filters.JWTRequestFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTRequestFilter jwtRequestFilter;
    private final AttemptsLimitFilter attemptsLimitFilter;
    private final CustomAuthenticationFailureHandler authFailureHandler;
    private final CustomAuthenticationSuccessHandler authSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomUsernamePasswordAuthenticationFilter loginFilter) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Unauthorized" + authException.getMessage());
                        })
                )
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(attemptsLimitFilter, JWTRequestFilter.class)
                .build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public CustomUsernamePasswordAuthenticationFilter loginFilter(AuthenticationManager authManager) {
        CustomUsernamePasswordAuthenticationFilter loginFilter =
                new CustomUsernamePasswordAuthenticationFilter(authSuccessHandler, authFailureHandler);
        loginFilter.setAuthenticationManager(authManager);
        return loginFilter;
    }

}
