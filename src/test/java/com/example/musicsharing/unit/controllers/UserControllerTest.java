package com.example.musicsharing.unit.controllers;

import com.example.musicsharing.controllers.UserController;
import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.UserInfoDTO;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.security.filters.AttemptsLimitFilter;
import com.example.musicsharing.security.filters.JWTRequestFilter;
import com.example.musicsharing.services.UserService;
import com.example.musicsharing.testsecurity.WithCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JWTRequestFilter filter;
    @MockitoBean
    private AttemptsLimitFilter attemptsLimitFilter;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithCustomUser
    void showUserInfo_shouldReturnUserInfoDto() throws Exception {
        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .username("testUser")
                .email("email")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        ApiResponse<UserInfoDTO> response = ApiResponse.success(userInfoDTO);
        String expectedJson = objectMapper.writeValueAsString(response);

        when(userService.showUserInfo()).thenReturn(userInfoDTO);

        mockMvc.perform(get("/api/users/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andExpect(jsonPath("$.errors").isEmpty());
    }


    @Test
    void getAllUsers_shouldReturnListUserInfoDto() throws Exception {
        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .username("username")
                .email("email")
                .build();
        UserInfoDTO userInfoDTO2 = UserInfoDTO.builder()
                .username("username")
                .email("email")
                .build();

        List<UserInfoDTO> userInfoDTOList = List.of(userInfoDTO, userInfoDTO2);
        ApiResponse<List<UserInfoDTO>> response = ApiResponse.success(userInfoDTOList);
        String expectedJson = objectMapper.writeValueAsString(response);

        when(userService.getAllUsers()).thenReturn(userInfoDTOList);

        mockMvc.perform(get("/api/users/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithCustomUser
    void updateUserInfo_shouldReturnUserInfoDto() throws Exception {
        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .username("username")
                .email("email@email.com")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        String username = "testUser";
        String requestBody = objectMapper.writeValueAsString(userInfoDTO);
        ApiResponse<UserInfoDTO> response = ApiResponse.success(userInfoDTO);
        String expectedJson = objectMapper.writeValueAsString(response);

        when(userService.updateUserInfo(eq(username), any(UserInfoDTO.class)))
                .thenReturn(userInfoDTO);

        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }
}