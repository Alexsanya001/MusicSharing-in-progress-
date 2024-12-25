package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.UserInfoDTO;
import com.example.musicsharing.security.JWTRequestFilter;
import com.example.musicsharing.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Mock
    private Principal principal;

    @Test
    void showUserInfo_shouldReturnUserInfoDto() throws Exception {
        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .username("username")
                .email("email")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        String username = "username";
        ApiResponse<UserInfoDTO> response = ApiResponse.success(userInfoDTO);
        String expectedJson = objectMapper.writeValueAsString(response);

        when(principal.getName()).thenReturn(username);
        when(userService.showUser(username)).thenReturn(userInfoDTO);

        mockMvc.perform(get("/api/users/info")
                .principal(principal)
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
}