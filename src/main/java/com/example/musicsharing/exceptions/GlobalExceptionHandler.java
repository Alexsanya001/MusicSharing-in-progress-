package com.example.musicsharing.exceptions;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import org.springframework.http.ResponseEntity;
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
}
