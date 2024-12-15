package com.example.musicsharing.controllers;

import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.services.UserService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<String> register
            (@RequestBody RegisterDTO registerDTO) {

        long id = userService.createUser(registerDTO);
        URI location;

        try {
            location = new URI("/api/users/" + id);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.created(location).build();
    }
}
