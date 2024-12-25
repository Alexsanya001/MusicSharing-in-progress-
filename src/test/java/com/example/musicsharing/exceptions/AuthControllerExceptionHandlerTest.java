package com.example.musicsharing.exceptions;

import com.example.musicsharing.controllers.AuthController;
import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.services.UserService;
import com.example.musicsharing.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JWTUtil jwtUtil;


    @Test
    void handleBadCredentialsException() throws Exception {
        LoginDTO loginDTO = LoginDTO.builder()
                .username("username")
                .password("password")
                .build();

        String requestBody = objectMapper.writeValueAsString(loginDTO);
        ErrorDetail errorDetail = new ErrorDetail("authentication", "Bad credentials");
        ApiResponse<?> expectedResponse = ApiResponse.failure(List.of(errorDetail));
        String expectedJson = objectMapper.writeValueAsString(expectedResponse);

        doThrow(BadCredentialsException.class)
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));


        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(expectedJson));
    }
}