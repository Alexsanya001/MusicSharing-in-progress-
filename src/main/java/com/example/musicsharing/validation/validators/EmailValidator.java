package com.example.musicsharing.validation.validators;

import com.example.musicsharing.models.entities.User;
import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.validation.annotations.ValidEmail;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;


@Component
@RequiredArgsConstructor
public class EmailValidator extends BasicValidator<ValidEmail> {

    private final UserRepository userRepository;

    @Value("${email.regexp}")
    private String regexValue;

    @Value("${email.message}")
    private String messageValue;

    @Value("${email.unique.message}")
    private String uniqueMessageValue;


    @Override
    public void initialize(ValidEmail annotation) {
        regex = regexValue;
        message = messageValue;
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!super.isValid(value, context)) {
            return false;
        }

        User storedUser = userRepository.findByEmail(value.toLowerCase()).orElse(null);
        if (storedUser != null && isDuplicate(storedUser)) {
                context.buildConstraintViolationWithTemplate(uniqueMessageValue).addConstraintViolation();
                return false;
            }
        return true;
    }
}
