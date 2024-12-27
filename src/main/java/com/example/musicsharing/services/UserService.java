package com.example.musicsharing.services;

import com.example.musicsharing.models.dto.ForgotPasswordDto;
import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.LoginResponseDto;
import com.example.musicsharing.models.dto.RestorePasswordRequest;
import com.example.musicsharing.models.dto.UserInfoDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    long createUser(RegisterDTO registerDTO);

    LoginResponseDto loginUser(LoginDTO loginDTO);

    UserInfoDTO showUser(String username);

    List<UserInfoDTO> getAllUsers();

    void sendRecoveryLink(ForgotPasswordDto forgotPasswordDto);

    void changePassword(RestorePasswordRequest request, String token);
    boolean validateToken(String token);
}
