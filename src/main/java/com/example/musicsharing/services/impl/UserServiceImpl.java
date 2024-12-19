package com.example.musicsharing.services.impl;

import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.UserInfoDTO;
import com.example.musicsharing.models.entities.Role;
import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.models.mappers.UserMapper;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.services.UserService;
import com.example.musicsharing.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt-exp-time}")
    private long tokenExpTime;


    @Override
    public long createUser(RegisterDTO registerDTO) {
        User user = userMapper.toUser(registerDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user).getId();
    }


    @Override
    public String createTokenOnLogin(LoginDTO loginDTO) {

        String username = loginDTO.getUsername();
        User user = findByUsername(username);
        long userId = user.getId();
        Map<String, String> claims = new HashMap<>();

        claims.put("username", username);
        claims.put("role", user.getRole().name());

        return jwtUtil.generateToken(Long.toString(userId), claims, tokenExpTime);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = findByUsername(username);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(
                        user.getRole().name()))
        );
    }

    @Override
    public UserInfoDTO showUser(String username) {
        User user = findByUsername(username);
        return userMapper.toUserInfoDTO(user);
    }


    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User %s not found", username)));
    }
}
