package com.example.musicsharing.validation.validators;

import com.example.musicsharing.validation.annotations.ValidName;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

public class NameValidator extends BasicValidator<ValidName>{

    @Value("${name.regexp}")
    private String regexValue;

    @Value("${name.message}")
    private String messageValue;


    @Override
    public void initialize(ValidName annotation) {
        regex = regexValue;
        message = messageValue;
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return super.isValid(value, context);
    }
}
