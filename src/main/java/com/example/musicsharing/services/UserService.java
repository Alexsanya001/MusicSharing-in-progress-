package com.example.musicsharing.services;

import com.example.musicsharing.models.dto.ForgotPasswordDto;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.RestorePasswordDto;
import com.example.musicsharing.models.dto.UserInfoDTO;

import java.util.List;

public interface UserService {
    long createUser(RegisterDTO registerDTO);

    UserInfoDTO showUser(String username);

    List<UserInfoDTO> getAllUsers();

    void sendRecoveryLink(ForgotPasswordDto forgotPasswordDto);

    void changePassword(RestorePasswordDto request, String token);

    boolean validateToken(String token);

    UserInfoDTO updateUserInfo(String username, UserInfoDTO updateUserDto);
}
