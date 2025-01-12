package com.example.musicsharing.services.impl;

import com.example.musicsharing.models.dto.ForgotPasswordDto;
import com.example.musicsharing.models.dto.RegisterDTO;
import com.example.musicsharing.models.dto.RestorePasswordDto;
import com.example.musicsharing.models.dto.UserInfoDTO;
import com.example.musicsharing.models.entities.Role;
import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.models.mappers.UserMapper;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.services.MailService;
import com.example.musicsharing.services.UserService;
import com.example.musicsharing.util.JWTUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    UserMapper userMapper;
    UserRepository userRepository;
    JWTUtil jwtUtil;
    PasswordEncoder passwordEncoder;
    MailService mailService;


    @Value("${jwt-short-exp-time}")
    @NonFinal
    Duration tokenShortExpTime;

    @Value("${domain}")
    @NonFinal
    String domain;

    static String RESTORE_PASSWORD_MESSAGE =
            """
                    <b>To reset your password click the button below:</b><br><br>
                    <form action="%s/api/auth/validate-token?token=%s" method="POST">
                        <button type="submit">Create a new password</button>
                    </form>""";


    @Override
    @Transactional
    public long createUser(RegisterDTO registerDTO) {
        User user = userMapper.toUser(registerDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user).getId();
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
            String message = String.format(RESTORE_PASSWORD_MESSAGE, domain, token);
            mailService.sendMail(email, "Restore password", message);
        }
    }


    @Override
    @Transactional
    public void changePassword(RestorePasswordDto requestBody, String token) {
        Long id = Long.parseLong(jwtUtil.extractClaim("sub", token));
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(requestBody.getNewPassword()));
            userRepository.save(user);
        }
    }


    @Override
    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }


    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User '%s' not found", username)));
    }
}
