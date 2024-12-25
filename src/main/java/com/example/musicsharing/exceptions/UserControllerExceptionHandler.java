package com.example.musicsharing.exceptions;

import com.example.musicsharing.controllers.UserController;
import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice(assignableTypes = UserController.class)
public class UserControllerExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUsernameNotFoundException(UsernameNotFoundException ex) {

        ErrorDetail errorDetail = new ErrorDetail("authentication", ex.getMessage());
        ApiResponse<?> apiResponse = ApiResponse.failure(List.of(errorDetail));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }
}
