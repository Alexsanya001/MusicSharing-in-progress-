package com.example.musicsharing.exceptions;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleJwtException(JwtException ex) {
        ErrorDetail errorDetail = new ErrorDetail("token", "");

        if (ex instanceof ExpiredJwtException) {
            errorDetail.setMessage("Token expired. Please try again.");
        } else {
            errorDetail.setMessage("Token invalid. Please try again.");
        }
        ApiResponse<ErrorDetail> apiResponse = ApiResponse.failure(List.of(errorDetail));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleRuntimeException(RuntimeException ex) {
        String message = "Internal Server Error";
        ErrorDetail errorDetail = new ErrorDetail("message", message);
        ApiResponse<ErrorDetail> apiResponse = ApiResponse.failure(List.of(errorDetail));

        return ResponseEntity.internalServerError().body(apiResponse);
    }
}
