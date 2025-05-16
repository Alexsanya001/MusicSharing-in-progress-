package com.example.musicsharing.security;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ResponseWrapper {

    ObjectMapper objectMapper;

    public void generateAuthFailureResponse(HttpServletResponse response, ErrorDetail errorDetail) {
        ApiResponse<?> apiResponse = ApiResponse.failure(List.of(errorDetail));
        generateResponse(response, apiResponse, HttpServletResponse.SC_UNAUTHORIZED);
    }

    public void generateAuthSuccessResponse(HttpServletResponse response, String token) {
        ApiResponse<String> apiResponse = ApiResponse.success(token);
        generateResponse(response, apiResponse, HttpServletResponse.SC_OK);
    }

    private void generateResponse(HttpServletResponse response, ApiResponse<?> apiResponse, int statusCode) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        try (var writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(apiResponse));
            writer.flush();
        } catch (IOException e) {
            log.error(e);
        }
    }
}
