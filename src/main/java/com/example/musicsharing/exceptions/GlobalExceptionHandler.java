package com.example.musicsharing.exceptions;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException
            (MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        List<ErrorDetail> errorDetails = new ArrayList<>();
        fieldErrors
                .forEach(fieldError ->
                        errorDetails.add(new ErrorDetail(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()))
                );
        ApiResponse<?> apiResponse = ApiResponse.failure(errorDetails);
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ErrorDetail errorDetail = new ErrorDetail("authorization", ex.getMessage() + ". Please contact your administrator.");
        ApiResponse<?> apiResponse = ApiResponse.failure(List.of(errorDetail));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
        String message = "Internal server error";
        ErrorDetail errorDetail = new ErrorDetail(null, message);
        ApiResponse<?> apiResponse = ApiResponse.failure(List.of(errorDetail));

        return ResponseEntity.internalServerError().body(apiResponse);
    }


    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<?>> handleJwtException(JwtException ex) {
        ErrorDetail errorDetail = new ErrorDetail("authentication", "");

        if (ex instanceof ExpiredJwtException) {
            errorDetail.setMessage("Token is expired. Please try again.");
        } else {
            errorDetail.setMessage("Token is invalid.");
        }
        ApiResponse<?> apiResponse = ApiResponse.failure(List.of(errorDetail));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }
}
