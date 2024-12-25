package com.example.musicsharing.exceptions;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ErrorDetail errorDetail = new ErrorDetail("authorization", ex.getMessage()+". Please contact your administrator.");
        ApiResponse<?> apiResponse = ApiResponse.failure(List.of(errorDetail));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
        String message = "Internal server error";
        ErrorDetail errorDetail = new ErrorDetail("message", message);
        ApiResponse<?> apiResponse = ApiResponse.failure(List.of(errorDetail));

        return ResponseEntity.internalServerError().body(apiResponse);
    }
}
