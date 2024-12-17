package com.example.musicsharing.validation.validators;

import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.validation.annotations.ValidUsernameNew;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class NewUsernameValidator extends BasicValidator<ValidUsernameNew> {

    private final UserRepository userRepository;

    @Value("${username.regexp}")
    private String regexValue;

    @Value("${new-username.message}")
    private String messageValue;

    @Value("${username.unique.message}")
    private String uniqueMessage;


    @Override
    public void initialize(ValidUsernameNew annotation) {
        regex = regexValue;
        message = messageValue;
        pattern = Pattern.compile(regex);
    }


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        boolean isValid = super.isValid(value, context);

        if (isValid) {
            if (userRepository.existsByUsername(value)) {
                isValid = false;
                context.buildConstraintViolationWithTemplate(uniqueMessage).addConstraintViolation();
            }
        }

        return isValid;
    }
}
