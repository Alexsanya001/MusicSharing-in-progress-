package com.example.musicsharing.services.impl;

import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.entities.Role;
import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.models.mappers.UserMapper;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public long createUser(RegisterDTO registerDTO) {
        User user = userMapper.toUser(registerDTO);
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user).getId();
    }
}
