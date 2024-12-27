package com.example.musicsharing.services.impl;

import com.example.musicsharing.models.dto.ForgotPasswordDto;
import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.models.dto.LoginResponseDto;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.RestorePasswordRequest;
import com.example.musicsharing.models.dto.UserInfoDTO;
import com.example.musicsharing.models.entities.Role;
import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.models.mappers.UserMapper;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.services.MailService;
import com.example.musicsharing.services.UserService;
import com.example.musicsharing.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${jwt-exp-time}")
    private Duration tokenExpTime;

    @Value("${jwt-short-exp-time}")
    private Duration tokenShortExpTime;

    @Value("${domain}")
    private String domain;

    public static final String RESTORE_PASSWORD_MESSAGE =
            "<b>To create a new password, follow the link below:</b>" +
                    "<br><br><a href=\"%s/api/auth/restore?token=%s\">" +
                    "Click here to reset your password</a>";


    @Override
    @Transactional
    public long createUser(RegisterDTO registerDTO) {
        User user = userMapper.toUser(registerDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user).getId();
    }


    @Override
    public LoginResponseDto loginUser(LoginDTO loginDTO) {

        String username = loginDTO.getUsername();
        User user = findByUsername(username);
        String token = createAuthToken(user);

        return LoginResponseDto.builder().token(token).build();
    }


    @Override
    public UserDetails loadUserByUsername(String username) {

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


    @Override
    public List<UserInfoDTO> getAllUsers() {
        return userMapper.toUserInfoDTOList(userRepository.findAll());
    }

    @Override
    public void sendRecoveryLink(ForgotPasswordDto forgotPasswordDto) {

        String email = forgotPasswordDto.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            String token = jwtUtil.generateToken(String.valueOf(user.getId()), tokenShortExpTime);
            //TODO Save token to Redis
            sendEmailAsync(email, token);
        }
    }


    @Async
    public void sendEmailAsync(String email, String token) {
        String message = String.format(RESTORE_PASSWORD_MESSAGE, domain, token);
        mailService.sendMail(email, "Restore password", message);
    }


    @Override
    @Transactional
    public void changePassword(RestorePasswordRequest request, String token) {
        Long id = Long.parseLong(jwtUtil.extractClaim("sub", token));
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        }
    }


    @Override
    public boolean validateToken(String token) {
        //TODO Check in Redis
        return jwtUtil.isTokenValid(token);
    }


    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User '%s' not found", username)));
    }

    private String createAuthToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole().name());
        return jwtUtil.generateToken(String.valueOf(user.getId()), claims, tokenExpTime);
    }
}
