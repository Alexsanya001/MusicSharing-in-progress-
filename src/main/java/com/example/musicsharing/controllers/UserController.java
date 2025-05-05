package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.UserInfoDTO;
import com.example.musicsharing.security.CustomUserDetails;
import com.example.musicsharing.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserInfoDTO>> showUserInfo() {
        UserInfoDTO userInfoDTO = userService.showUserInfo();
        ApiResponse<UserInfoDTO> response = ApiResponse.success(userInfoDTO);
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
