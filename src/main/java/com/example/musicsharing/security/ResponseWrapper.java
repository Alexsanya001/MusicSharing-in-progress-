package com.example.musicsharing.security;

import com.example.musicsharing.models.dto.ApiResponse;
import com.example.musicsharing.models.dto.ErrorDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public final class ResponseWrapper {

    public static void generateAuthFailureResponse(HttpServletResponse response, ErrorDetail errorDetail) {
        ApiResponse<?> apiResponse = ApiResponse.failure(List.of(errorDetail));
        ObjectMapper mapper = new ObjectMapper();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (var writer = response.getWriter()) {
            writer.write(mapper.writeValueAsString(apiResponse));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
