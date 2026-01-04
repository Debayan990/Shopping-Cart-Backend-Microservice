package com.cts.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 1. Check if we stored a specific error message in the Filter
        String errorMessage = (String) request.getAttribute("authError");

        // 2. If no specific message, use the default Spring Security message
        if (errorMessage == null) {
            errorMessage = authException.getMessage();
        }

        // Using simple string concatenation for the JSON output
        String jsonOutput = String.format("{\"message\": \"Unauthorized\", \"error\": \"%s\"}", errorMessage);

        // 3. Write the JSON
        PrintWriter out = response.getWriter();
        out.write(jsonOutput);

    }
}
