package com.example.musicsharing.security;

import com.example.musicsharing.models.dto.AuthUserDto;
import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.models.mappers.UserMapper;
import com.example.musicsharing.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUserDto dto = userRepository.findAuthUserDtoByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username.formatted("User %s not found")));

        User user = mapper.toUser(dto);
        return new CustomUserDetails(user);
    }
}
