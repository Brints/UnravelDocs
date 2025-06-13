package com.extractor.unraveldocs.security;

import com.extractor.unraveldocs.global.response.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());

        UserResponse<Void> errorResponse = new UserResponse<>();
        errorResponse.setStatusCode(HttpStatus.FORBIDDEN.value());
        errorResponse.setStatus("FORBIDDEN");
        errorResponse.setMessage("Only admins can access this resource.");
        errorResponse.setData(null);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}