package com.cts.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Get Token from Header
        String token = getTokenFromRequest(request);

        try{
            // 2. Validate Token
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

                // 3. Extract Username and Roles
                String username = jwtTokenProvider.getUsername(token);
                List<String> roles = jwtTokenProvider.getRoles(token); // You might need to add getRoles() to JwtTokenProvider

                // 4. Convert Roles to Authorities
                List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority(role)) // Role is typically "ROLE_ADMIN" or "ROLE_USER"
                        .collect(Collectors.toList());

                // 5. Create Authentication Object
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Set Security Context
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        } catch (Exception e) {
        // Catch the specific JWT exception (Expired, Malformed, etc.)
        // and store the message in the request for the EntryPoint to read.
        request.setAttribute("authError", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}