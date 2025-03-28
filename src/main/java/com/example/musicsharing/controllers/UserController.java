package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.UserInfoDTO;
import com.example.musicsharing.security.CustomUserDetails;
import com.example.musicsharing.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoDTO>> showUserInfo(@AuthenticationPrincipal CustomUserDetails principal) {
        UserInfoDTO userInfoDTO = userService.showUser(principal.getName());
        ApiResponse<UserInfoDTO> response = ApiResponse.success(userInfoDTO);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserInfoDTO>>> getAllUsers() {
        List<UserInfoDTO> users = userService.getAllUsers();
        ApiResponse<List<UserInfoDTO>> response = ApiResponse.success(users);
        return ResponseEntity.ok(response);
    }


    @PutMapping
    public ResponseEntity<ApiResponse<UserInfoDTO>> updateUserInfo(
            @RequestBody @Valid UserInfoDTO updateUserDto, @AuthenticationPrincipal CustomUserDetails principal) {
        UserInfoDTO updatedUser = userService.updateUserInfo(principal.getName(), updateUserDto);
        ApiResponse<UserInfoDTO> response = ApiResponse.success(updatedUser);
        return ResponseEntity.ok(response);
    }

}
