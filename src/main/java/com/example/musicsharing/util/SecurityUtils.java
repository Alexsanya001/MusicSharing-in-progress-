package com.example.musicsharing.util;

import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.security.CustomUserDetails;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@UtilityClass
public class SecurityUtils {

    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails;
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            userDetails = (CustomUserDetails) authentication.getPrincipal();
            return Optional.of(userDetails.user());
        }
        return Optional.empty();
    }

}
