package com.example.musicsharing.exceptions;

import com.example.musicsharing.controllers.UserController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UserController.class)
public class UserControllerExceptionHandler {
}
