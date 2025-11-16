package com.voxever.teammies.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        new ObjectMapper().writeValue(response.getOutputStream(), generateResponseBody(request, authException));
    }

    private Map<String, Object> generateResponseBody(HttpServletRequest request, AuthenticationException authException){
        Map<String, Object> responseBody = new HashMap<>();

        responseBody.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        responseBody.put("message", authException.getMessage());
        responseBody.put("path", request.getServletPath());

        return responseBody;
    }
}