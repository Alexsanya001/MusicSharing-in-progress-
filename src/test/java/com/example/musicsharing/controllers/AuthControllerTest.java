package com.example.musicsharing.controllers;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JWTUtil jwtUtil;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private AttemptsLimitService attemptsLimitService;
    @MockitoBean
    private RequestDataExtractor requestDataExtractor;


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


//    @Test
//    void login_shouldReturnToken_whenUserIsAuthenticated() throws Exception {
//        LoginDTO loginDTO = LoginDTO.builder()
//                .username("username")
//                .password("password")
//                .build();
//        LoginResponseDto token = LoginResponseDto.builder().token("token").build();
//        Authentication authentication = mock(Authentication.class);
//        ApiResponse<LoginResponseDto> response = ApiResponse.success(token);
//        String requestBody = objectMapper.writeValueAsString(loginDTO);
//        String expectedJson = objectMapper.writeValueAsString(response);
//
//        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
//        when(userService.loginUser(any(LoginDTO.class))).thenReturn(token);
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk())
//                .andExpect(content().json(expectedJson))
//                .andExpect(jsonPath("$.errors").isEmpty());
//
//    }
}