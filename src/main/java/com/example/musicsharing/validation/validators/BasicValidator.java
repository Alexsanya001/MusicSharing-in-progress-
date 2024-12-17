package com.example.musicsharing.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

public abstract class BasicValidator<A extends Annotation> implements ConstraintValidator<A, String> {

    protected String regex;
    protected String message;
    protected Pattern pattern;


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        if (value == null || !pattern.matcher(value).matches()) {
            isValid = false;
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }

        return isValid;
    }
}
