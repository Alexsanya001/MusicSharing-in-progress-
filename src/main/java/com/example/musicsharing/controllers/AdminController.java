package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.UserInfoDTO;
import com.example.musicsharing.services.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin-panel")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {

    UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/all")
    public ResponseEntity<ApiResponse<List<UserInfoDTO>>> getAllUsers() {
        List<UserInfoDTO> users = userService.getAllUsers();
        ApiResponse<List<UserInfoDTO>> response = ApiResponse.success(users);
        return ResponseEntity.ok(response);
    }
}
