package com.example.musicsharing.services;

import com.example.musicsharing.models.dto.ForgotPasswordDto;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.RestorePasswordDto;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    @Mock
    private MailService mailService;

    @InjectMocks
    private UserServiceImpl userServiceImpl;
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;
    @Captor
    private ArgumentCaptor<String> emailCaptor;
    @Captor
    private ArgumentCaptor<String> subjectCaptor;
    @Captor
    private ArgumentCaptor<String> messageCaptor;


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
    void showUser_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.showUser("username"));
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
    void sendRecoveryLink_shouldSendLinkToExistingUser() {
        ForgotPasswordDto dto = ForgotPasswordDto.builder().email("test@email.com").build();
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("generated-token");

        userService.sendRecoveryLink(dto);

        verify(mailService).sendMail(emailCaptor.capture(), subjectCaptor.capture(), messageCaptor.capture());

        assertEquals(dto.getEmail(), emailCaptor.getValue());
        assertEquals("Restore password", subjectCaptor.getValue());
        assertTrue(messageCaptor.getValue().contains("?token=generated-token"));
    }


    @Test
    void sendRecoveryLink_shouldNotSend_whenEmailIsWrong() {
        ForgotPasswordDto dto = ForgotPasswordDto.builder().email("not-existing@email.com").build();
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        userService.sendRecoveryLink(dto);

        verify(mailService, never()).sendMail(anyString(), anyString(), anyString());
    }


    @Test
    void changePassword_shouldChangePassword() {
        RestorePasswordDto dto = RestorePasswordDto.builder().newPassword("New-password1").build();
        String token = "token";
        User user = new User();
        user.setId(1L);
        user.setPassword("Old-password");

        when(jwtUtil.extractClaim("sub", token)).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(dto.getNewPassword())).thenReturn("New-password1-encoded");

        userService.changePassword(dto, token);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("New-password1-encoded", capturedUser.getPassword());
    }


    @Test
    void changePassword_shouldNotChangePassword_whenTokenIsWrong() {
        RestorePasswordDto dto = RestorePasswordDto.builder().newPassword("New-password1").build();
        String token = "token";

        when(jwtUtil.extractClaim("sub", token)).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        userService.changePassword(dto, token);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }


    @Test
    void validateToken_shouldValidateToken() {
        when(jwtUtil.isTokenValid(anyString())).thenReturn(true);
        boolean result = userService.validateToken("token");
        assertTrue(result);
    }


    @Test
    void updateUserInfo_shouldReturnUpdatedUserInfoDto() {
        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .username("username")
                .email("email@email.com")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        UserInfoDTO updated = UserInfoDTO.builder()
                .username("username")
                .email("email@email.com")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        User toUpdate = new User();

        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(toUpdate));
        when(userRepository.save(toUpdate))
                .thenReturn(toUpdate);
        when(userMapper.toUserInfoDTO(toUpdate))
                .thenReturn(updated);

        UserInfoDTO result = userService.updateUserInfo("username", userInfoDTO);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("username", capturedUser.getUsername());
        assertEquals("email@email.com", capturedUser.getEmail());
        assertEquals("firstName", capturedUser.getFirstName());
        assertEquals("lastName", capturedUser.getLastName());

        assertEquals(userInfoDTO.getUsername(), result.getUsername());
        assertEquals(userInfoDTO.getEmail(), result.getEmail());
        assertEquals(userInfoDTO.getFirstName(), result.getFirstName());
        assertEquals(userInfoDTO.getLastName(), result.getLastName());
    }
}