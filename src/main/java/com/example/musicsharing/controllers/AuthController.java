package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ForgotPasswordDto;
import com.example.musicsharing.models.dto.ForgotPasswordResponse;
import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.models.dto.LoginResponseDto;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.RestorePasswordRequest;
import com.example.musicsharing.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String PASSWORD_RECOVERY_MESSAGE =
            "Instructions for password recovering were sent to %s";

    private final UserService userService;
    private final AuthenticationManager authManager;


    @PostMapping("/register")
    public ResponseEntity<String> register
            (@Valid @RequestBody RegisterDTO registerDTO) {

        long id = userService.createUser(registerDTO);

        URI location = UriComponentsBuilder
                .fromPath("/api/users/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginDTO loginDTO) {

        authManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword()));

        LoginResponseDto token = userService.loginUser(loginDTO);
        ApiResponse<LoginResponseDto> response = ApiResponse.success(token);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> sendRecoveryLink(
            @RequestBody ForgotPasswordDto forgotPasswordDto) {

        userService.sendRecoveryLink(forgotPasswordDto);

        String message = String.format(PASSWORD_RECOVERY_MESSAGE, forgotPasswordDto.getEmail());
        ForgotPasswordResponse forgotPasswordResponse = ForgotPasswordResponse
                .builder().message(message).build();

        ApiResponse<ForgotPasswordResponse> response = ApiResponse.success(forgotPasswordResponse);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/restore-password")
    public ResponseEntity<ApiResponse<String>> restorePassword(
            @RequestParam String token,
            @RequestBody @Valid RestorePasswordRequest request) {

        if (userService.validateToken(token)) {
            userService.changePassword(request, token);
        }

        ApiResponse<String> response = ApiResponse.success("Password successfully changed");

        return ResponseEntity.ok(response);
    }
}
