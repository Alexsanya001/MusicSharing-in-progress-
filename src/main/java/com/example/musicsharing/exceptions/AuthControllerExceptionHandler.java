package com.example.musicsharing.exceptions;

import com.example.musicsharing.controllers.AuthController;
import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthControllerExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleMethodArgumentNotValidException
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

        ApiResponse<ErrorDetail> apiResponse = ApiResponse.failure(errorDetails);

        return ResponseEntity.badRequest().body(apiResponse);
    }


    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleUsernameNotFoundException(UsernameNotFoundException ex) {

        ErrorDetail errorDetail = new ErrorDetail("authentication", ex.getMessage());
        ApiResponse<ErrorDetail> apiResponse = ApiResponse.failure(List.of(errorDetail));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorDetail errorDetail = new ErrorDetail("authentication", "Bad credentials");
        ApiResponse<ErrorDetail> apiResponse = ApiResponse.failure(List.of(errorDetail));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }
}
