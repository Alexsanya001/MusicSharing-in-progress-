package com.example.musicsharing.validation.validators;

import com.example.musicsharing.validation.annotations.ValidPasswordNew;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

public class PasswordValidator extends BasicValidator<ValidPasswordNew>{

    @Value("${password.regexp}")
    String regexValue;

    @Value("${new-password.message}")
    String messageValue;


    @Override
    public void initialize(ValidPasswordNew annotation) {
        regex = regexValue;
        message = messageValue;
        pattern = Pattern.compile(regex);
    }
}
