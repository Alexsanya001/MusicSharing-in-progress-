package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

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
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginDTO loginDTO) {

        authManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword()));

        String token = userService.createTokenOnLogin(loginDTO);
        ApiResponse<String> response = ApiResponse.success(token);

        return ResponseEntity.ok(response);
    }
}
