package com.example.musicsharing.exceptions;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.security.filters.AttemptsLimitFilter;
import com.example.musicsharing.services.UserService;
import com.example.musicsharing.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JWTUtil jwtUtil;
    @MockitoBean
    private AttemptsLimitFilter attemptsLimitFilter;


    @Test
    void handleRuntimeExceptionTest() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        ErrorDetail errorDetail = new ErrorDetail("message", "Internal server error");
        ApiResponse<?> expectedResponse = ApiResponse.failure(List.of(errorDetail));

        doThrow(RuntimeException.class).when(userService).createUser(any(RegisterDTO.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }
}