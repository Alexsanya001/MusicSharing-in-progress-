package com.example.musicsharing.exceptions;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.security.AttemptsLimitService;
import com.example.musicsharing.services.UserService;
import com.example.musicsharing.util.JWTUtil;
import com.example.musicsharing.util.RequestDataExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private JWTUtil jwtUtil;
    @MockitoBean
    private AttemptsLimitService attemptsLimitService;
    @MockitoBean
    RequestDataExtractor requestDataExtractor;
    @MockitoBean
    UserRepository repository;


    @Test
    void handleRuntimeExceptionTest() throws Exception {
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("username")
                .password("Password1")
                .email("email@email.com")
                .firstName("firstName")
                .lastName("lastName")
                .build();
        ErrorDetail errorDetail = new ErrorDetail(null, "Internal server error");
        ApiResponse<?> expectedResponse = ApiResponse.failure(List.of(errorDetail));

        doThrow(RuntimeException.class).when(userService).createUser(any(RegisterDTO.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }


    @Test
    void handleAuthorizationDeniedExceptionTest() throws Exception {
        ErrorDetail errorDetail = new ErrorDetail("authorization",
                "Access denied. Please contact your administrator."
        );
        ApiResponse<?> expectedResponse = ApiResponse.failure(List.of(errorDetail));
        String expectedJson = objectMapper.writeValueAsString(expectedResponse);

        doNothing().when(attemptsLimitService).incrementLoginAttempts(anyString());
        doThrow(new AuthorizationDeniedException("Access denied")).when(userService).getAllUsers();

        mockMvc.perform(get("/api/users/all"))
                .andExpect(status().isForbidden())
                .andExpect(content().json(expectedJson));
    }
}