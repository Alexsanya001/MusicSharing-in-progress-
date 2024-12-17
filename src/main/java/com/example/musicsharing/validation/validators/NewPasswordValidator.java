package com.example.musicsharing.validation.validators;

import com.example.musicsharing.validation.annotations.ValidPasswordNew;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

public class NewPasswordValidator extends BasicValidator<ValidPasswordNew>{

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

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context){
        return super.isValid(value, context);
    }

}
