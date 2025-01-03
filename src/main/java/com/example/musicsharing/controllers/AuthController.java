package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ForgotPasswordDto;
import com.example.musicsharing.models.dto.ForgotPasswordResponse;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.RestorePasswordDto;
import com.example.musicsharing.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    static String PASSWORD_RECOVERY_MESSAGE =
            "Instructions for password recovering were sent to %s";
    static String CHANGE_PASSWORD_SUCCESS_MESSAGE =
            "Password successfully changed";

    UserService userService;


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


    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        Boolean isValid = userService.validateToken(token);
        ApiResponse<Boolean> response = ApiResponse.success(isValid);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> restorePassword(
            @RequestBody @Valid RestorePasswordDto restorePasswordDto, HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        userService.changePassword(restorePasswordDto, token);

        ApiResponse<String> response = ApiResponse.success(CHANGE_PASSWORD_SUCCESS_MESSAGE);
        return ResponseEntity.ok(response);
    }
}
