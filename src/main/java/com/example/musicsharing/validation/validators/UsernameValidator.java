package com.example.musicsharing.validation.validators;

import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.validation.annotations.ValidUsername;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class UsernameValidator extends BasicValidator<ValidUsername> {

    private final UserRepository userRepository;

    @Value("${username.regexp}")
    private String regexValue;

    @Value("${new-username.message}")
    private String messageValue;

    @Value("${username.unique.message}")
    private String uniqueMessage;


    @Override
    public void initialize(ValidUsername annotation) {
        regex = regexValue;
        message = messageValue;
        pattern = Pattern.compile(regex);
    }


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!super.isValid(value, context)) {
            return false;
        }

        User storedUser = userRepository.findByUsername(value).orElse(null);
        if (storedUser != null && isDuplicate(storedUser)) {
                context.buildConstraintViolationWithTemplate(uniqueMessage).addConstraintViolation();
                return false;
            }
        return true;
    }
}
