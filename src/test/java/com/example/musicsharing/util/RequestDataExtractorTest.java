package com.example.musicsharing.util;

import com.example.musicsharing.models.dto.LoginDTO;
import com.example.musicsharing.security.CustomHttpServletRequestWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestDataExtractorTest {

    @Mock
    JWTUtil jwtUtil;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CustomHttpServletRequestWrapper requestWrapper;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    RequestDataExtractor requestDataExtractor;


    @Test
    void extractLoginData_ShouldReturnLoginDTO_WhenRequestIsCustomWrapper() throws IOException {
        String requestBody = "{\"username\":\"testUser\",\"password\":\"testPassword\"}";
        when(requestWrapper.getBody()).thenReturn(requestBody);

        JsonNode jsonNode = mock(JsonNode.class);
        JsonNode usernameNode = mock(JsonNode.class);
        JsonNode passwordNode = mock(JsonNode.class);

        when(objectMapper.readTree(requestBody)).thenReturn(jsonNode);
        when(jsonNode.get("username")).thenReturn(usernameNode);
        when(jsonNode.get("password")).thenReturn(passwordNode);
        when(usernameNode.asText()).thenReturn("testUser");
        when(passwordNode.asText()).thenReturn("testPassword");

        LoginDTO loginDTO = requestDataExtractor.extractLoginData(requestWrapper);

        assertNotNull(loginDTO);
        assertEquals("testUser", loginDTO.getUsername());
        assertEquals("testPassword", loginDTO.getPassword());
    }


    @Test
    void extractLoginData_ShouldReturnNull_WhenRequestIsNotCustomWrapper() throws IOException {
        LoginDTO loginDTO = requestDataExtractor.extractLoginData(request);

        assertNull(loginDTO);
    }


    @Test
    void extractUserId_ShouldReturnUserId_WhenAuthorizationHeaderIsValid() {
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtUtil.extractClaim("sub", "validToken")).thenReturn("1");

        String userId = requestDataExtractor.extractUserId(request);

        assertEquals("1", userId);
    }


    @Test
    void extractUserId_ShouldReturnUnknown_WhenAuthorizationHeaderIsMissing() {
        when(request.getHeader("Authorization")).thenReturn(null);

        String userId = requestDataExtractor.extractUserId(request);

        assertEquals("Unknown", userId);
    }


    @Test
    void extractUserId_ShouldReturnUnknown_WhenAuthorizationHeaderDoesNotStartWithBearer() {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader validToken");

        String userId = requestDataExtractor.extractUserId(request);

        assertEquals("Unknown", userId);
    }


    @Test
    void extractUserId_ShouldReturnUnknown_WhenJwtUtilThrowsException() {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        when(jwtUtil.extractClaim("sub", "invalidToken")).thenThrow(new JwtException("Invalid token"));

        String userId = requestDataExtractor.extractUserId(request);

        assertEquals("Unknown", userId);
    }
}
