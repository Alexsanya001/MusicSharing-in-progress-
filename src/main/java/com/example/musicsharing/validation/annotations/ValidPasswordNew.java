package com.example.musicsharing.validation.annotations;

import com.example.musicsharing.validation.validators.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPasswordNew {

    String message() default "Invalid password provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
