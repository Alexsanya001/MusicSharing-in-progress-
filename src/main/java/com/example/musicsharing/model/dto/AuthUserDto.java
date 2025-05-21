package com.example.musicsharing.models.dto;

import com.example.musicsharing.models.entities.Role;

import java.time.Instant;

public record AuthUserDto(long id,
                          String username,
                          String password,
                          String email,
                          Role role,
                          Instant passwordChangedAt) {
}
