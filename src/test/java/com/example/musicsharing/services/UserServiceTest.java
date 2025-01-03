package com.example.musicsharing.services;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.LoginResponseDto;
import com.example.musicsharing.models.dto.UserInfoDTO;
import com.example.musicsharing.models.entities.Role;
import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.models.mappers.UserMapper;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.services.impl.UserServiceImpl;
import com.example.musicsharing.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JWTUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userServiceImpl;
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;
    @Captor
    private ArgumentCaptor<String> idCaptor;
    @Captor
    private ArgumentCaptor<Map<String, Object>> claimsCaptor;
    @Captor
    private ArgumentCaptor<Duration> timeCaptor;


    @BeforeEach
    void setUp() {
        userService = userServiceImpl;
    }

    @Test
    void createUser_shouldSaveUserAndReturnUserId() {
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("username")
                .password("password")
                .build();

        User user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setId(1L);

        String encodedPassword = "encodedPassword";

        when(userMapper.toUser(registerDTO)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(userRepository.save(user)).thenReturn(user);

        long userId = userService.createUser(registerDTO);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("username", capturedUser.getUsername());
        assertEquals(encodedPassword, capturedUser.getPassword());
        assertEquals(userId, capturedUser.getId());
        assertEquals(1L, capturedUser.getId());
        assertEquals(Role.ROLE_USER, capturedUser.getRole());
    }

//    @Test
//    void loginUser_shouldReturnToken() {
//        LoginDTO loginDTO = LoginDTO.builder()
//                .username("username")
//                .build();
//
//        User user = new User();
//        user.setUsername("username");
//        user.setRole(Role.ROLE_USER);
//        user.setId(1L);
//
//        when(userRepository.findByUsername(loginDTO.getUsername()))
//                .thenReturn(Optional.of(user));
//        when(jwtUtil.generateToken(anyString(), anyMap(), any(Duration.class)))
//                .thenReturn("token");
//
//        ReflectionTestUtils.setField(userServiceImpl, "tokenExpTime", Duration.ofMinutes(1));
//
//        LoginResponseDto result = userService.loginUser(loginDTO);
//
//        verify(jwtUtil).generateToken(idCaptor.capture(), claimsCaptor.capture(), timeCaptor.capture());
//        Map<String, Object> claims = claimsCaptor.getValue();
//
//        assertEquals("token", result.getToken());
//        assertEquals(idCaptor.getValue(), String.valueOf(user.getId()));
//        assertEquals(claims.get("username"), user.getUsername());
//        assertEquals(claims.get("role"), Role.ROLE_USER.toString());
//        assertEquals(Duration.ofMinutes(1L), timeCaptor.getValue());
//    }

//    @Test
//    void loginUser_shouldThrowException_whenUserNotFound() {
//        LoginDTO loginDTO = LoginDTO.builder()
//                .username("username")
//                .build();
//
//        when(userRepository.findByUsername(loginDTO.getUsername()))
//                .thenReturn(Optional.empty());
//
//        assertThrows(UsernameNotFoundException.class,
//                () -> userService.loginUser(loginDTO));
//    }

    @Test
    void showUser_shouldReturnUserInfoDto() {
        User user = new User();
        user.setUsername("username");

        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .username("username")
                .build();

        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(user));
        when(userMapper.toUserInfoDTO(user)).thenReturn(userInfoDTO);

        UserInfoDTO expected = userService.showUser("username");
        assertEquals(expected, userInfoDTO);
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User user = new User();
        user.setUsername("username");
        List<User> users = List.of(user);

        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .username("username")
                .build();

        when(userRepository.findAll())
                .thenReturn(users);
        when(userMapper.toUserInfoDTOList(users))
                .thenReturn(List.of(userInfoDTO));

        List<UserInfoDTO> expected = userService.getAllUsers();

        assertIterableEquals(expected, List.of(userInfoDTO));
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
//        User user = new User();
//        user.setUsername("username");
//        user.setPassword("password");
//        user.setRole(Role.ROLE_USER);
//
//        when(userRepository.findByUsername(anyString()))
//                .thenReturn(Optional.of(user));
//
//        //UserDetails expected = userService.loadUserByUsername("username");
//
//        assertEquals(expected.getUsername(), user.getUsername());
//        assertEquals(expected.getPassword(), user.getPassword());
//        assertEquals("ROLE_USER", expected.getAuthorities().stream().findFirst().get().getAuthority());
    }
}