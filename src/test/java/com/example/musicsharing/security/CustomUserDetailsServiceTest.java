package com.example.musicsharing.security;

import com.example.musicsharing.models.entities.Role;
import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;


    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
                User user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setRole(Role.ROLE_USER);

        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(user));

        UserDetails expected = customUserDetailsService.loadUserByUsername("username");

        assertEquals(expected.getUsername(), user.getUsername());
        assertEquals(expected.getPassword(), user.getPassword());
        assertEquals("ROLE_USER", expected.getAuthorities().stream().findFirst().get().getAuthority());
    }
}