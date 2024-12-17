package com.example.musicsharing.validation.validators;

import com.example.musicsharing.repositories.UserRepository;
import com.example.musicsharing.validation.annotations.ValidEmailNew;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class NewEmailValidator extends BasicValidator<ValidEmailNew> {

    private final UserRepository userRepository;

    @Value("${email.regexp}")
    private String regexValue;

    @Value("${email.message}")
    private String messageValue;

    @Value("${email.unique.message}")
    private String uniqueMessageValue;


    @Override
    public void initialize(ValidEmailNew annotation) {
        regex = regexValue;
        message = messageValue;
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        boolean isValid = super.isValid(value, context);

        if (isValid) {
            if (userRepository.existsByEmail(value)) {
                isValid = false;
                context.buildConstraintViolationWithTemplate(uniqueMessageValue).addConstraintViolation();
            }
        }

        return isValid;
    }
}
