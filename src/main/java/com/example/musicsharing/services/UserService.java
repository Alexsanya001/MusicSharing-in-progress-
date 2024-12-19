package com.example.musicsharing.services;

import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.UserInfoDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    long createUser(RegisterDTO registerDTO);

    String createTokenOnLogin(LoginDTO loginDTO);

    UserInfoDTO showUser(String username);

    List<UserInfoDTO> getAllUsers();
}
