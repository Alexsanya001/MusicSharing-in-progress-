package com.example.musicsharing.validation.annotations;

import com.example.musicsharing.validation.validators.NewUsernameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NewUsernameValidator.class)
public @interface ValidUsernameNew {

    String message() default "Invalid username provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
