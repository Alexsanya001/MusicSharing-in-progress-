package com.example.musicsharing.services;

import com.example.musicsharing.models.dto.RegisterDTO;

public interface UserService {
    long createUser(RegisterDTO registerDTO);
}
