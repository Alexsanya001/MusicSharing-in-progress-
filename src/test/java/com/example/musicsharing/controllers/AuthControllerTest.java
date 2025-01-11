package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ForgotPasswordDto;
import com.example.musicsharing.models.dto.ForgotPasswordResponse;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.RestorePasswordDto;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.security.AttemptsLimitService;
import com.example.musicsharing.services.UserService;
import com.example.musicsharing.util.JWTUtil;
import com.example.musicsharing.util.RequestDataExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JWTUtil jwtUtil;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private AttemptsLimitService attemptsLimitService;
    @MockitoBean
    private RequestDataExtractor requestDataExtractor;
    @Mock
    HttpServletRequest httpServletRequest;


    @Test
    void register_returnsLocation_whenUserIsRegistered() throws Exception {
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("Valid_username")
                .password("Valid_password1")
                .email("Valid@email.com")
                .firstName("Valid First name")
                .lastName("Valid Last name")
                .build();

        long userId = 1L;
        String requestBody = objectMapper.writeValueAsString(registerDTO);

        when(userRepository.existsByUsername(registerDTO.getUsername()))
                .thenReturn(false);
        when(userRepository.existsByEmail(registerDTO.getEmail()))
                .thenReturn(false);
        when(userService.createUser(any(RegisterDTO.class)))
                .thenReturn(userId);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                ).andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/" + userId));
    }


    @Test
    void register_returnsFailure_whenFieldsAreNotValid() throws Exception {
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username(" ")
                .password("invalid_password")
                .email("not-valid_email.com")
                .firstName("Invalid1")
                .lastName("Invalid2")
                .build();

        String requestBody = objectMapper.writeValueAsString(registerDTO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.size()").value(5));

        verify(userService, never()).createUser(any());
    }


    @Test
    void register_returnsFailure_whenUserOrEmailExists() throws Exception {
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("existent")
                .password("Password123")
                .email("existent@email.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        String requestBody = objectMapper.writeValueAsString(registerDTO);

        when(userRepository.existsByUsername(registerDTO.getUsername().toLowerCase()))
                .thenReturn(true);
        when(userRepository.existsByEmail(registerDTO.getEmail().toLowerCase()))
                .thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", containsInAnyOrder(
                        Map.of("field", "username", "message", "Username already taken."),
                        Map.of("field", "email", "message", "Email already in use.")
                )));

        verify(userService, never()).createUser(any());
    }


    @Test
    void sendRecoveryLink_shouldReturnApiResponseWithMessage() throws Exception {
        ForgotPasswordDto forgotPasswordDto = ForgotPasswordDto.builder().email("test@email.com").build();
        String requestBody = objectMapper.writeValueAsString(forgotPasswordDto);
        String message = String.format("Instructions for password recovering were sent to %s",
                forgotPasswordDto.getEmail());

        ForgotPasswordResponse forgotPasswordResponse = ForgotPasswordResponse
                .builder().message(message).build();
        ApiResponse<ForgotPasswordResponse> response = ApiResponse.success(forgotPasswordResponse);
        String responseBody = objectMapper.writeValueAsString(response);

        doNothing().when(userService).sendRecoveryLink(forgotPasswordDto);

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));

        verify(userService).sendRecoveryLink(any(ForgotPasswordDto.class));
    }


    @Test
    void validateToken_shouldReturnApiResponseWithSuccess() throws Exception {
        String token = "token";
        Boolean isValid = true;
        when(userService.validateToken(token))
                .thenReturn(isValid);

        ApiResponse<Boolean> response = ApiResponse.success(isValid);
        String responseBody = objectMapper.writeValueAsString(response);

        mockMvc.perform(post("/api/auth/validate-token")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));

        verify(userService).validateToken(token);
    }


    @Test
    void restorePassword_shouldReturnApiResponseWithSuccess() throws Exception {
        RestorePasswordDto restorePasswordDto = RestorePasswordDto.builder()
                .newPassword("ValidPassword1").build();
        String requestBody = objectMapper.writeValueAsString(restorePasswordDto);
        String token = "token";
        String header = "Bearer " + token;
        String successMessage = "Password successfully changed";

        doNothing().when(userService).changePassword(restorePasswordDto, token);

        mockMvc.perform(post("/api/auth/reset-password")
                .header("Authorization", header)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(successMessage));

        verify(userService).changePassword(any(RestorePasswordDto.class), eq(token));
    }
}